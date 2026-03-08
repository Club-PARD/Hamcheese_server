package pard.server.com.longkathon.config.webSocket.chatRoom;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pard.server.com.longkathon.MyPage.user.User;
import pard.server.com.longkathon.MyPage.user.UserRepo;
import pard.server.com.longkathon.MyPage.userFile.UserFileService;
import pard.server.com.longkathon.config.webSocket.chatMessage.ChatMessageRepository;
import pard.server.com.longkathon.config.webSocket.dto.*;
import pard.server.com.longkathon.util.TimeUtils;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final UserRepo userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserFileService userFileService;  // 프로필 이미지 URL

    @Transactional
    public ChatRoomResponse createChatRoom(Long userId, Long partnerId) {
        if (userId.equals(partnerId)) {
            throw new IllegalArgumentException("INVALID_CHAT_REQUEST");
        }

        userRepository.findById(partnerId).orElseThrow(()-> new IllegalArgumentException("USER_NOT_FOUND"));
        Optional<ChatRoom> chatRoom = chatRoomRepository.findChatRoomByUsers(userId, partnerId);

        if (chatRoom.isPresent()) { //채팅방이 존재한다면, 존재하는 채팅방을 리턴
            Long chatRoomId = chatRoom.get().getId();
            List<ChatMessageResponse> messages = chatMessageRepository.findMessagesWithUserByChatRoomId(chatRoomId);
            return ChatRoomResponse.fromEntity(chatRoom.get(), messages);
        }

        ChatRoom newChatRoom = chatRoomRepository.save(new ChatRoom(userId, partnerId));
        return ChatRoomResponse.fromEntity(newChatRoom, Collections.emptyList());
    }

    @Transactional
    public List<ChatRoomListResponse> getMyChatRooms(Long myId) {
        List<ChatRoomWithLastMessageDto> rooms = chatRoomRepository
            .findMyRoomsWithLastMessage(myId);

        if (rooms.isEmpty()) return List.of();

        // 대화 상대 userId 추출
        List<Long> partnerIds = rooms.stream()
            .map(room -> room.getUserId().equals(myId) ? room.getPartnerId() : room.getUserId())
            .distinct()
            .toList();

        // 대화 상대 정보 조회 (N+1 방지)
        Map<Long, User> partnersById = userRepository.findAllById(partnerIds).stream()
            .collect(Collectors.toMap(User::getUserId, u -> u));

        return rooms.stream()
            .map(room -> {
                Long partnerId = room.getUserId().equals(myId) ? room.getPartnerId() : room.getUserId();
                User partner = partnersById.get(partnerId);

                return ChatRoomListResponse.builder()
                    .chatRoomId(room.getChatRoomId())
                    .partnerId(partnerId)
                    .partnerName(partner.getName())
                    .partnerImageUrl(userFileService.getURL(partnerId))
                    .lastMessage(room.getLastMessage())
                    .timeAgo(room.getLastMessageTime() != null
                        ? TimeUtils.toRelativeTime(room.getLastMessageTime())
                        : null)
                    .lastMessageTime(room.getLastMessageTime())
                    .build();
            })
            .toList();
    }

    @Transactional
    public ChatRoomDetailResponse getChatRoomDetail(Long chatRoomId, Long myId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new IllegalArgumentException("CHAT_ROOM_NOT_FOUND"));

        // 권한 확인
        if (!chatRoom.getUserId().equals(myId) && !chatRoom.getPartnerId().equals(myId)) {
            throw new IllegalArgumentException("UNAUTHORIZED_ACCESS");
        }

        // 대화 상대 ID 확인
        Long partnerId = chatRoom.getUserId().equals(myId)
            ? chatRoom.getPartnerId()
            : chatRoom.getUserId();

        // 대화 상대 정보 조회
        User partner = userRepository.findById(partnerId)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        // 메시지 목록 조회
        List<ChatMessageResponse> messages = chatMessageRepository
            .findMessagesWithUserByChatRoomId(chatRoomId);

        // 시간 포맷팅
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN);
        List<ChatMessageWithTimeResponse> formattedMessages = messages.stream()
            .map(msg -> ChatMessageWithTimeResponse.builder()
                .messageId(msg.getMessageId())
                .senderId(msg.getSenderId())
                .senderName(msg.getNickname())
                .content(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .formattedTime(msg.getCreatedAt().format(timeFormatter))
                .build())
            .toList();

        return ChatRoomDetailResponse.builder()
            .chatRoomId(chatRoomId)
            .partner(ChatRoomDetailResponse.PartnerInfo.builder()
                .userId(partner.getUserId())
                .name(partner.getName())
                .studentId(partner.getStudentId())
                .imageUrl(userFileService.getURL(partnerId))
                .firstMajor(partner.getFirstMajor())
                .secondMajor(partner.getSecondMajor())
                .build())
            .messages(formattedMessages)
            .build();
    }
}

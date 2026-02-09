package pard.server.com.longkathon.config.webSocket.chatRoom;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pard.server.com.longkathon.MyPage.user.UserRepo;
import pard.server.com.longkathon.config.webSocket.chatMessage.ChatMessageRepository;
import pard.server.com.longkathon.config.webSocket.dto.ChatMessageResponse;
import pard.server.com.longkathon.config.webSocket.dto.ChatRoomResponse;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final UserRepo userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatRoomResponse createChatRoom(Long userId, Long sellerId) {
        if (userId.equals(sellerId)) {
            throw new IllegalArgumentException("INVALID_CHAT_REQUEST");
        }

        userRepository.findById(sellerId).orElseThrow(()-> new IllegalArgumentException("USER_NOT_FOUND"));
        Optional<ChatRoom> chatRoom = chatRoomRepository.findChatRoomByUsers(userId, sellerId);

        if (chatRoom.isPresent()) {
            Long chatRoomId = chatRoom.get().getId();
            List<ChatMessageResponse> messages = chatMessageRepository.findMessagesWithUserByChatRoomId(chatRoomId);
            return ChatRoomResponse.fromEntity(chatRoom.get(), messages);
        }

        ChatRoom newChatRoom = chatRoomRepository.save(new ChatRoom(userId, sellerId));
        return ChatRoomResponse.fromEntity(newChatRoom, Collections.emptyList());
    }
}

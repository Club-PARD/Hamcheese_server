package pard.server.com.longkathon.config.webSocket.chatMessage;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pard.server.com.longkathon.MyPage.user.User;
import pard.server.com.longkathon.MyPage.user.UserRepo;
import pard.server.com.longkathon.config.webSocket.chatRoom.ChatRoom;
import pard.server.com.longkathon.config.webSocket.chatRoom.ChatRoomRepository;
import pard.server.com.longkathon.config.webSocket.dto.ChatMessageRequest;
import pard.server.com.longkathon.config.webSocket.dto.ChatMessageResponse;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final UserRepo userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatMessageResponse createChatMessage(ChatMessageRequest req, Long senderId) {
        ChatRoom chatRoom = chatRoomRepository.findById(req.getChatRoomId())
                .orElseThrow(()-> new IllegalArgumentException("INVALID_CHAT_REQUEST"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(()->new IllegalArgumentException("USER_NOT_FOUND"));
        ChatMessage message = new ChatMessage(chatRoom, senderId, req.getContent());
        chatMessageRepository.save(message);

        ChatMessageResponse response = ChatMessageResponse.fromEntity(message, sender);
        response.setNickname(sender.getName());

        return response;
    }
}

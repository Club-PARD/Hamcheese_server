package pard.server.com.longkathon.config.webSocket.chatMessage;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
import pard.server.com.longkathon.config.webSocket.dto.ChatMessageRequest;
import pard.server.com.longkathon.config.webSocket.dto.ChatMessageResponse;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    @MessageMapping("/message")
    public void sendMessage(ChatMessageRequest req,
                            SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        ChatMessageResponse response = chatMessageService.createChatMessage(req, userId);
        messagingTemplate.convertAndSend("/sub/channel/" + req.getChatRoomId(), response);
    }
}

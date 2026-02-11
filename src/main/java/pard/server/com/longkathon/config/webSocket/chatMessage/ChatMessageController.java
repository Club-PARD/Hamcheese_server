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
        // // 1. 세션에서 userId 꺼냄 (StompHandler가 CONNECT 때 저장한 것)
        Long userId = (Long) accessor.getSessionAttributes().get("userId");

        // 2. 메시지를 DB에 저장하고 응답 객체 생성
        ChatMessageResponse response = chatMessageService.createChatMessage(req, userId);

        //3. 해당 채팅방을 구독 중인 모든 사용자에게 브로드캐스트
        messagingTemplate.convertAndSend("/sub/channel/" + req.getChatRoomId(), response);
    }
}

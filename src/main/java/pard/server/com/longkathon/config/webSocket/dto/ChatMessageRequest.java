package pard.server.com.longkathon.config.webSocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequest {
    private Long chatRoomId;
    private String content;
}

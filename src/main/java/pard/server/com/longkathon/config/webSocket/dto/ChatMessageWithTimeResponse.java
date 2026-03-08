package pard.server.com.longkathon.config.webSocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageWithTimeResponse {
    private Long messageId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;
    private String formattedTime;  // "오후 1:34"
}

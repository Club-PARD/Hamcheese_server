package pard.server.com.longkathon.config.webSocket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatRoomWithLastMessageDto {
    private Long chatRoomId;
    private Long userId;
    private Long partnerId;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
}

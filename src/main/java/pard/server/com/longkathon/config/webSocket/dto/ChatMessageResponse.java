package pard.server.com.longkathon.config.webSocket.dto;
import lombok.*;
import pard.server.com.longkathon.MyPage.user.User;
import pard.server.com.longkathon.config.webSocket.chatMessage.ChatMessage;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    private Long messageId;
    private Long chatRoomId;
    private Long senderId;
    private String content;
    private String nickname;      // service에서 setNickname() 하는 필드
    private LocalDateTime createdAt;

    public static ChatMessageResponse fromEntity(ChatMessage message, User sender) {
        return new ChatMessageResponse(
                message.getId(),
                message.getChatRoomId(),
                message.getSenderId(),      // ChatMessage에 senderId 필드가 있다고 가정
                message.getContent(),
                null,                       // 너 코드에서 setNickname() 하니까 여기서는 null로 둠
                message.getCreatedAt()      // BaseEntity Auditing 사용 시
        );
    }
}

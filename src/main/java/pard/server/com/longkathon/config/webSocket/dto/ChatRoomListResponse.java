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
public class ChatRoomListResponse {
    private Long chatRoomId;
    private Long partnerId;           // 대화 상대 ID
    private String partnerName;       // 대화 상대 이름
    private String partnerImageUrl;   // 프로필 사진
    private String lastMessage;       // 마지막 메시지
    private String timeAgo;           // "3분전"
    private LocalDateTime lastMessageTime;  // 정렬용
}

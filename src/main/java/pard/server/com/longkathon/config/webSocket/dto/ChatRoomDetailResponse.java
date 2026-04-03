package pard.server.com.longkathon.config.webSocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDetailResponse {
    private Long chatRoomId;
    private PartnerInfo partner;
    private List<ChatMessageWithTimeResponse> messages;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PartnerInfo {
        private Long userId;
        private String name;
        private Long studentId;      // 학번
        private String imageUrl;
        private String firstMajor;
        private String secondMajor;
    }
}

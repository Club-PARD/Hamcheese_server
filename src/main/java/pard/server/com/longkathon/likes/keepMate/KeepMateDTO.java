package pard.server.com.longkathon.likes.keepMate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class KeepMateDTO {

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long keepUserId; // 찜한 유저 ID
        private boolean isKeepMate; // 찜 상태
        private String message; // 응답 메시지
    }
}

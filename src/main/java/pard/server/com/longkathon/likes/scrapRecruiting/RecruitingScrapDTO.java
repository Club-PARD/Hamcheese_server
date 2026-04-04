package pard.server.com.longkathon.likes.scrapRecruiting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class RecruitingScrapDTO {

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long recruitingId;
        private Boolean isScrap;
        private Long scrapCount;
        private String message;
    }
}

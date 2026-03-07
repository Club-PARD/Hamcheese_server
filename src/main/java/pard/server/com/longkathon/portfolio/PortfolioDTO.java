package pard.server.com.longkathon.portfolio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PortfolioDTO {
    @AllArgsConstructor
    @Builder
    @Getter
    @NoArgsConstructor
    public static class Res1 {
        private String title;

        private LocalDateTime postDate; // 작성 날짜

        private String imageUrl; //썸네일 url
    }
}

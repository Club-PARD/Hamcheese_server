package pard.server.com.longkathon.portfolio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PortfolioDTO {
    @AllArgsConstructor
    @Builder
    @Getter
    @NoArgsConstructor
    public static class Res1 {
        private String title;

        private Long portfolioId;

        private LocalDateTime postDate; // 작성 날짜

        private String imageUrl; //썸네일 url
    }

    @AllArgsConstructor
    @Builder
    @Getter
    @NoArgsConstructor
    public static class Res2 {
        private Long portfolioId;

        private String title;

        private String organization;

        private String category; //기여 분야

        private LocalDateTime startDate; // 시작 날짜

        private LocalDateTime endDate; // 종료날짜

        private String description;

        private List<String> linkList;

        private List<String> hashtagList;

        private List<String> imageUrlList;
    }

}

package pard.server.com.longkathon.portfolio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PortfolioDTO {
    @AllArgsConstructor
    @Builder
    @Getter
    @NoArgsConstructor
    public static class Res1 {
        private String title;

        private String postDate; // 작성 날짜

        private String imageUrl; //썸네일 url
    }
}

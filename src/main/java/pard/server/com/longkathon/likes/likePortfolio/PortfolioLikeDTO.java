package pard.server.com.longkathon.likes.likePortfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PortfolioLikeDTO {

    /**
     * 좋아요 생성/삭제 응답 DTO
     */
    @AllArgsConstructor
    @Builder
    @Getter
    @NoArgsConstructor
    public static class Response {
        private Long portfolioId;      // 포트폴리오 ID
        private boolean isLiked;       // 현재 좋아요 상태 (true: 좋아요됨, false: 취소됨)
        private Long likeCount;        // 현재 총 좋아요 수
        private String message;        // 사용자에게 보여줄 메시지
    }

    /**
     * 좋아요 수 조회 응답 DTO
     */
    @AllArgsConstructor
    @Builder
    @Getter
    @NoArgsConstructor
    public static class CountResponse {
        private Long portfolioId;      // 포트폴리오 ID
        private Long likeCount;        // 좋아요 수
    }

    /**
     * 좋아요 상태 조회 응답 DTO
     */
    @AllArgsConstructor
    @Builder
    @Getter
    @NoArgsConstructor
    public static class StatusResponse {
        private Long portfolioId;      // 포트폴리오 ID
        private boolean isLiked;       // 현재 사용자의 좋아요 여부
    }
}

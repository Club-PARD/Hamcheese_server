package pard.server.com.longkathon.likes.likePortfolio;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portfolioLike")
@RequiredArgsConstructor
public class PortfolioLikeController {
    private final PortfolioLikeService portfolioLikeService;

    /**
     * 포트폴리오 좋아요 추가
     * POST /portfolioLike/{portfolioId}
     */
    @PostMapping("/{portfolioId}")
    public ResponseEntity<PortfolioLikeDTO.Response> createLike(@PathVariable Long portfolioId) {
        PortfolioLikeDTO.Response response = portfolioLikeService.createPortfolioLike(portfolioId);
        return ResponseEntity.ok(response);
    }

    /**
     * 포트폴리오 좋아요 삭제
     * DELETE /portfolioLike/{portfolioId}
     */
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<PortfolioLikeDTO.Response> deleteLike(@PathVariable Long portfolioId) {
        PortfolioLikeDTO.Response response = portfolioLikeService.deletePortfolioLike(portfolioId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 포트폴리오의 좋아요 수 조회
     * GET /portfolioLike/count/{portfolioId}
     */
    @GetMapping("/count/{portfolioId}")
    public ResponseEntity<PortfolioLikeDTO.CountResponse> getLikeCount(@PathVariable Long portfolioId) {
        Long likeCount = portfolioLikeService.getLikeCount(portfolioId);

        PortfolioLikeDTO.CountResponse response = PortfolioLikeDTO.CountResponse.builder()
                .portfolioId(portfolioId)
                .likeCount(likeCount)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인된 사용자의 좋아요 상태 확인
     * GET /portfolioLike/status/{portfolioId}
     */
    @GetMapping("/status/{portfolioId}")
    public ResponseEntity<PortfolioLikeDTO.StatusResponse> getLikeStatus(@PathVariable Long portfolioId) {
        boolean isLiked = portfolioLikeService.isLikedByCurrentUser(portfolioId);

        PortfolioLikeDTO.StatusResponse response = PortfolioLikeDTO.StatusResponse.builder()
                .portfolioId(portfolioId)
                .isLiked(isLiked)
                .build();

        return ResponseEntity.ok(response);
    }
}

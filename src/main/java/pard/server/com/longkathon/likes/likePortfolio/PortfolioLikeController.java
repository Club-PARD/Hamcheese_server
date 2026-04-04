package pard.server.com.longkathon.likes.likePortfolio;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pard.server.com.longkathon.MyPage.user.AuthorizeUserId;
import pard.server.com.longkathon.portfolio.PortfolioDTO;
import pard.server.com.longkathon.portfolio.PortfolioService;

import java.util.List;

@RestController
@RequestMapping("/portfolioLike")
@RequiredArgsConstructor
public class PortfolioLikeController {
    private final PortfolioLikeService portfolioLikeService;
    private final PortfolioService portfolioService;

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
     * 현재 로그인한 유저가 좋아요 누른 포트폴리오 리스트 조회
     * GET /portfolioLike/findAll
     */
    @GetMapping("/findAll")
    public ResponseEntity<List<PortfolioDTO.Res1>> findAll() {
        Long userId = AuthorizeUserId.getAuthorizedUserId();
        List<PortfolioDTO.Res1> likedPortfolios = portfolioService.getLikedPortfolios(userId);
        return ResponseEntity.ok(likedPortfolios);
    }
}

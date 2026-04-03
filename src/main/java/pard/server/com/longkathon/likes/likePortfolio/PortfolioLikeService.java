package pard.server.com.longkathon.likes.likePortfolio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pard.server.com.longkathon.MyPage.user.AuthorizeUserId;
import pard.server.com.longkathon.common.exception.DuplicateLikeException;
import pard.server.com.longkathon.common.exception.LikeNotFoundException;
import pard.server.com.longkathon.common.exception.PortfolioNotFoundException;
import pard.server.com.longkathon.portfolio.PortfolioRepository;

@Service
@RequiredArgsConstructor
public class PortfolioLikeService {
    private final PortfolioLikeRepository portfolioLikeRepository;
    private final PortfolioRepository portfolioRepository;

    /**
     * 포트폴리오 좋아요 생성
     */
    @Transactional
    public PortfolioLikeDTO.Response createPortfolioLike(Long portfolioId) {
        // 1. 로그인된 사용자 ID 획득
        Long userId = AuthorizeUserId.getAuthorizedUserId();

        // 2. Portfolio 존재 여부 확인
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new PortfolioNotFoundException("포트폴리오를 찾을 수 없습니다. ID: " + portfolioId);
        }

        // 3. 중복 확인
        if (portfolioLikeRepository.existsByUserIdAndPortfolioId(userId, portfolioId)) {
            throw new DuplicateLikeException("이미 좋아요를 누른 포트폴리오입니다.");
        }

        // 4. 좋아요 생성
        PortfolioLike portfolioLike = PortfolioLike.builder()
                .userId(userId)
                .portfolioId(portfolioId)
                .build();
        portfolioLikeRepository.save(portfolioLike);

        // 5. 응답 생성
        Long likeCount = portfolioLikeRepository.countByPortfolioId(portfolioId);
        return PortfolioLikeDTO.Response.builder()
                .portfolioId(portfolioId)
                .isLiked(true)
                .likeCount(likeCount)
                .message("좋아요가 추가되었습니다.")
                .build();
    }

    /**
     * 포트폴리오 좋아요 삭제
     */
    @Transactional
    public PortfolioLikeDTO.Response deletePortfolioLike(Long portfolioId) {
        // 1. 로그인된 사용자 ID 획득
        Long userId = AuthorizeUserId.getAuthorizedUserId();

        // 2. 존재 여부 확인
        if (!portfolioLikeRepository.existsByUserIdAndPortfolioId(userId, portfolioId)) {
            throw new LikeNotFoundException("좋아요 기록을 찾을 수 없습니다.");
        }

        // 3. 좋아요 삭제
        portfolioLikeRepository.deleteByUserIdAndPortfolioId(userId, portfolioId);

        // 4. 응답 생성
        Long likeCount = portfolioLikeRepository.countByPortfolioId(portfolioId);
        return PortfolioLikeDTO.Response.builder()
                .portfolioId(portfolioId)
                .isLiked(false)
                .likeCount(likeCount)
                .message("좋아요가 취소되었습니다.")
                .build();
    }

    /**
     * 특정 포트폴리오의 좋아요 수 조회
     */
    public Long getLikeCount(Long portfolioId) {
        return portfolioLikeRepository.countByPortfolioId(portfolioId);
    }

    /**
     * 현재 사용자의 좋아요 상태 확인
     */
    public boolean isLikedByCurrentUser(Long portfolioId) {
        Long userId = AuthorizeUserId.getAuthorizedUserId();
        return portfolioLikeRepository.existsByUserIdAndPortfolioId(userId, portfolioId);
    }
}

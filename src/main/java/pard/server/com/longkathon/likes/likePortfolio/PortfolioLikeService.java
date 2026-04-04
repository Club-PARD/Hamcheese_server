package pard.server.com.longkathon.likes.likePortfolio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pard.server.com.longkathon.MyPage.user.AuthorizeUserId;
import pard.server.com.longkathon.common.exception.PortfolioNotFoundException;
import pard.server.com.longkathon.portfolio.PortfolioRepository;

@Service
@RequiredArgsConstructor
public class PortfolioLikeService {
    private final PortfolioLikeRepository portfolioLikeRepository;
    private final PortfolioRepository portfolioRepository;

    /**
     * 포트폴리오 좋아요 토글 (추가/취소)
     */
    @Transactional
    public PortfolioLikeDTO.Response togglePortfolioLike(Long portfolioId) {
        // 1. 인증된 사용자 ID 획득
        Long userId = AuthorizeUserId.getAuthorizedUserId();

        // 2. Portfolio 존재 확인
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new PortfolioNotFoundException("포트폴리오를 찾을 수 없습니다. ID: " + portfolioId);
        }

        // 3. 좋아요 여부에 따라 생성/삭제
        boolean isLiked;
        String message;

        if (portfolioLikeRepository.existsByUserIdAndPortfolioId(userId, portfolioId)) {
            // 좋아요 취소
            portfolioLikeRepository.deleteByUserIdAndPortfolioId(userId, portfolioId);
            isLiked = false;
            message = "좋아요가 취소되었습니다.";
        } else {
            // 좋아요 추가
            PortfolioLike portfolioLike = PortfolioLike.builder()
                    .userId(userId)
                    .portfolioId(portfolioId)
                    .build();
            portfolioLikeRepository.save(portfolioLike);
            isLiked = true;
            message = "좋아요가 추가되었습니다.";
        }

        // 4. 응답 생성
        Long likeCount = portfolioLikeRepository.countByPortfolioId(portfolioId);
        return PortfolioLikeDTO.Response.builder()
                .portfolioId(portfolioId)
                .isLiked(isLiked)
                .likeCount(likeCount)
                .message(message)
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

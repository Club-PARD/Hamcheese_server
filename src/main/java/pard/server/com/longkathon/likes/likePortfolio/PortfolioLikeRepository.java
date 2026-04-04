package pard.server.com.longkathon.likes.likePortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PortfolioLikeRepository extends JpaRepository<PortfolioLike, Long> {

    /**
     * 좋아요 존재 여부 확인 (중복 생성 방지용)
     */
    boolean existsByUserIdAndPortfolioId(Long userId, Long portfolioId);

    /**
     * 특정 사용자-포트폴리오 조합의 좋아요 삭제
     */
    @Transactional
    void deleteByUserIdAndPortfolioId(Long userId, Long portfolioId);

    /**
     * 특정 포트폴리오의 총 좋아요 수 조회
     */
    @Query("SELECT COUNT(pl) FROM PortfolioLike pl WHERE pl.portfolioId = :portfolioId")
    Long countByPortfolioId(@Param("portfolioId") Long portfolioId);

    /**
     * N+1 문제 해결: 여러 포트폴리오의 좋아요 수를 한 번에 조회
     * @param portfolioIds 좋아요 수를 조회할 포트폴리오 ID 리스트
     * @return Object[] 배열의 리스트 - [0]: portfolioId (Long), [1]: count (Long)
     */
    @Query("SELECT pl.portfolioId, COUNT(pl) " +
           "FROM PortfolioLike pl " +
           "WHERE pl.portfolioId IN :portfolioIds " +
           "GROUP BY pl.portfolioId")
    List<Object[]> countLikesByPortfolioIds(@Param("portfolioIds") List<Long> portfolioIds);

    /**
     * 특정 유저가 좋아요 누른 포트폴리오 ID 리스트 조회 (최신순)
     * PortfolioLikeId 내림차순 = 최근 좋아요한 순서
     */
    @Query("SELECT pl.portfolioId FROM PortfolioLike pl " +
           "WHERE pl.userId = :userId " +
           "ORDER BY pl.PortfolioLikeId DESC")
    List<Long> findPortfolioIdsByUserId(@Param("userId") Long userId);
}

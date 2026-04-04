package pard.server.com.longkathon.portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long>, JpaSpecificationExecutor<Portfolio> {
    List<Portfolio> findAllByUserId(Long userId);

    // 생성일 기준 최신순 정렬 (BaseEntity의 createdAt 필드 사용)
    List<Portfolio> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // 프로젝트 시작일 기준 최신순 정렬
    List<Portfolio> findAllByUserIdOrderByStartDateDesc(Long userId);

    /**
     * 여러 Portfolio ID로 Portfolio 엔티티 리스트 조회
     */
    List<Portfolio> findAllByPortfolioIdIn(List<Long> portfolioIds);
}

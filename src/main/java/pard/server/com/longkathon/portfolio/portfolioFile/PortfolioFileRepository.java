package pard.server.com.longkathon.portfolio.portfolioFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioFileRepository extends JpaRepository<PortfolioFile, Long>{
    Optional<PortfolioFile> findByPortfolioIdAndIsThumbnail(Long portfolioId, boolean isThumbnail);

    // 특정 포트폴리오의 모든 파일 조회
    List<PortfolioFile> findAllByPortfolioId(Long portfolioId);
}

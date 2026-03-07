package pard.server.com.longkathon.portfolio.portfolioFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

@Repository
public interface PortfolioFileRepository extends JpaRepository<PortfolioFile, Long>{
    Optional<PortfolioFile> findByPortfolioIdAndIsThumbnail(Long portfolioId, boolean isThumbnail);
}

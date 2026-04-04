package pard.server.com.longkathon.portfolio.portfolioURL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioURLRepository extends JpaRepository<PortfolioURL, Long> {

    List<PortfolioURL> findAllByPortfolioId(Long portfolioId);

    void deleteAllByPortfolioId(Long portfolioId);
}

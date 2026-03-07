package pard.server.com.longkathon.portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long>{
}

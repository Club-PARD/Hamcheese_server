package pard.server.com.longkathon.portfolio.hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    void deleteAllByPortfolioId(Long portfolioId);
}

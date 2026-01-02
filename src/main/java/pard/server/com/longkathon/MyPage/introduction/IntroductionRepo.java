package pard.server.com.longkathon.MyPage.introduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntroductionRepo extends JpaRepository<Introduction, Long> {
}

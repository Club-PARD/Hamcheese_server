package pard.server.com.longkathon.MyPage.peerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pard.server.com.longkathon.MyPage.introduction.Introduction;

@Repository
public interface PeerReviewRepo extends JpaRepository<PeerReview, Long>{

}

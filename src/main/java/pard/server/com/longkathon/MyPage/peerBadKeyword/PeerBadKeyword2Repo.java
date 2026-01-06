package pard.server.com.longkathon.MyPage.peerBadKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pard.server.com.longkathon.MyPage.peerGoodKeyword.PeerGoodKeyword;

import java.util.Collection;
import java.util.List;

@Repository
public interface PeerBadKeyword2Repo extends JpaRepository<PeerBadKeyword2, Long>{
    List<PeerBadKeyword2> findAllByUserIdAndPeerReviewId(Long userId, Long peerReviewId);
}

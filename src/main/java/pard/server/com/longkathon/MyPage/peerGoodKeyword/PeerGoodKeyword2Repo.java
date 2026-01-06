package pard.server.com.longkathon.MyPage.peerGoodKeyword;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeerGoodKeyword2Repo  extends JpaRepository<PeerGoodKeyword2, Long> {
    List<PeerGoodKeyword2> findAllByUserIdAndPeerReviewId(Long userId, Long peerReviewId);
}

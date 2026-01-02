package pard.server.com.longkathon.MyPage.peerBadKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pard.server.com.longkathon.MyPage.introduction.Introduction;
import pard.server.com.longkathon.MyPage.peerGoodKeyword.PeerGoodKeyword;

import java.util.List;

@Repository
public interface PeerBadKeywordRepo extends JpaRepository<PeerBadKeyword, Long>{
    List<PeerBadKeyword> findTop3ByUserIdOrderByCountDesc(Long userId);
    List<PeerBadKeyword> findByUserId(Long userId);
}

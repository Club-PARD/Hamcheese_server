package pard.server.com.longkathon.MyPage.peerGoodKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeerGoodKeywordRepo extends JpaRepository<PeerGoodKeyword, Long>{
    List<PeerGoodKeyword> findTop3ByUserIdOrderByCountDesc(Long userId);

    List<PeerGoodKeyword> findByUserId(Long userId);
}

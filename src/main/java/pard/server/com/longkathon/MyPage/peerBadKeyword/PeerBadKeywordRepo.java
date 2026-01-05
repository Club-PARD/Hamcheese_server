package pard.server.com.longkathon.MyPage.peerBadKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pard.server.com.longkathon.MyPage.introduction.Introduction;
import pard.server.com.longkathon.MyPage.peerGoodKeyword.PeerGoodKeyword;

import java.util.Collection;
import java.util.List;

@Repository
public interface PeerBadKeywordRepo extends JpaRepository<PeerBadKeyword, Long>{
    List<PeerBadKeyword> findByUserId(Long userId);

    List<PeerBadKeyword> findAllByUserIdOrderByCountDesc(Long userId); //해당 유저의 모든 부정키워드를 count순서대로 리턴

    List<PeerBadKeyword> findAllByUserIdAndKeywordIn(Long userId, Collection<String> keywords);

    List<PeerBadKeyword> findAllByUserId(Long userId);
}

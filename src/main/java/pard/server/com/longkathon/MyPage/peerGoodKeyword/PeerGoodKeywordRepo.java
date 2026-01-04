package pard.server.com.longkathon.MyPage.peerGoodKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PeerGoodKeywordRepo extends JpaRepository<PeerGoodKeyword, Long>{
    List<PeerGoodKeyword> findByUserId(Long userId); //해당 유저의 모든 긍정키워드를 리스트로리턴

    List<PeerGoodKeyword> findAllByUserIdOrderByCountDesc(Long userId);

    List<PeerGoodKeyword> findTop3ByUserIdOrderByCountDesc(Long userId); //해당 유저의 top3 긍정키워드를 리스트로리턴

    List<PeerGoodKeyword> findAllByUserIdAndKeywordIn(Long userId, Collection<String> keywords);

}

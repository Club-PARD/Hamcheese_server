package pard.server.com.longkathon.MyPage.peerReview;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import pard.server.com.longkathon.MyPage.peerBadKeyword.PeerBadKeyword;
import pard.server.com.longkathon.MyPage.peerBadKeyword.PeerBadKeywordRepo;
import pard.server.com.longkathon.MyPage.peerGoodKeyword.PeerGoodKeyword;
import pard.server.com.longkathon.MyPage.peerGoodKeyword.PeerGoodKeywordRepo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PeerReviewService {
    private final PeerReviewRepo peerReviewRepo;
    private final PeerGoodKeywordRepo peerGoodKeywordRepo;
    private final PeerBadKeywordRepo peerBadKeywordRepo;

    public Map<String, Integer> GoodKeyword (Long userId){
        List<PeerGoodKeyword> top3 = peerGoodKeywordRepo.findTop3ByUserIdOrderByCountDesc(userId);
        //count의 개수가 가장많은 상위 3개의 PeerGoodKeyword를 가져온다.

        Map<String, Integer> result = new HashMap<>();
        //가져온 엔티티들의 key-value페어를 keyword - count로 맞춘 맵을 형성

        for (PeerGoodKeyword e : top3) {
            result.put(e.getKeyword(), e.getCount()); // keyword(String) -> count(int)
        }
        return result;
    }

    public int goodKeywordCount (Long userId){
        int total = 0;
        //해당 유저에 대한 모든 긍정키워드 엔티티들를 찾는다.
        List<PeerGoodKeyword> goodKeywordList = peerGoodKeywordRepo.findByUserId(userId);

        //그렇게 찾은 엔티티들의 count필드를 더한다.
        for (PeerGoodKeyword e : goodKeywordList) {
            total += e.getCount();
        }

        return total;
    }

    public Map<String, Integer> BadKeyword (Long userId){
        List<PeerBadKeyword> top3 = peerBadKeywordRepo.findTop3ByUserIdOrderByCountDesc(userId);
        //count의 개수가 가장많은 상위 3개의 PeerBadKeyword 가져온다.

        Map<String, Integer> result = new HashMap<>();
        //가져온 엔티티들의 key-value페어를 keyword - count로 맞춘 맵을 형성

        for (PeerBadKeyword e : top3) {
            result.put(e.getKeyword(), e.getCount()); // keyword(String) -> count(int)
        }
        return result;
    }

    public int badKeywordCount (Long userId){
        int total = 0;
        //해당 유저에 대한 모든 부정 키워드 엔티티들를 찾는다.
        List<PeerBadKeyword> badKeywordList = peerBadKeywordRepo.findByUserId(userId);

        //그렇게 찾은 엔티티들의 count필드를 더한다.
        for (PeerBadKeyword e : badKeywordList) {
            total += e.getCount();
        }

        return total;
    }
}

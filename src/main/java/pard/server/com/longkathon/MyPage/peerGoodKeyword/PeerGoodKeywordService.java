package pard.server.com.longkathon.MyPage.peerGoodKeyword;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
public class PeerGoodKeywordService {
    private final PeerGoodKeyword2Repo peerGoodKeyword2Repo;

    public List<String> readKeyword(Long userId, Long peerReviewId) { //긍정키워드만 스트링으로 뽑은 리스트
        return peerGoodKeyword2Repo.findAllByUserIdAndPeerReviewId(userId, peerReviewId).stream()
                .map(PeerGoodKeyword2::getKeyword)
                .toList(); // Java 16+ (Java 8~15면 Collectors.toList())
    }
}

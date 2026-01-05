package pard.server.com.longkathon.MyPage.peerGoodKeyword;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
public class PeerGoodKeywordSevice {
    private final PeerGoodKeywordRepo peerGoodKeywordRepo;

    public List<String> readKeyword(Long userId) { //긍정키워드만 스트링으로 뽑은 리스트
        return peerGoodKeywordRepo.findAllByUserId(userId).stream()
                .map(PeerGoodKeyword::getKeyword)
                .toList(); // Java 16+ (Java 8~15면 Collectors.toList())
    }
}

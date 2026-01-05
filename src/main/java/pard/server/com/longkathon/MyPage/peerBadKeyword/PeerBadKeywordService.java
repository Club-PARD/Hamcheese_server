package pard.server.com.longkathon.MyPage.peerBadKeyword;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
public class PeerBadKeywordService {
    private final PeerBadKeywordRepo peerBadKeywordRepo;

    public List<String> readKeyword(Long userId) { //긍정키워드만 스트링으로 뽑은 리스트
        return peerBadKeywordRepo.findAllByUserId(userId).stream()
                .map(PeerBadKeyword::getKeyword)
                .toList(); // Java 16+ (Java 8~15면 Collectors.toList())
    }
}
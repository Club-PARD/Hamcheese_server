package pard.server.com.longkathon.portfolio.hashtag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import pard.server.com.longkathon.portfolio.Portfolio;

import java.util.List;


@Service
@RequiredArgsConstructor
public class HashtagService {
    private final HashtagRepository hashtagRepository;

    //hashtag 저장
    public void save(List<String> hashtagList, Long portfolioId) {
        List<Hashtag> entityList = hashtagList.stream()
                .map(hashtagName -> Hashtag.builder()
                        .portfolioId(portfolioId)
                        .hashtagName(hashtagName)
                        .build())
                .toList();
        hashtagRepository.saveAll(entityList);
    }

    //hashtag삭제
    @Transactional
    public void delete(Long portfolioId) {
        hashtagRepository.deleteAllByPortfolioId(portfolioId);
    }
}

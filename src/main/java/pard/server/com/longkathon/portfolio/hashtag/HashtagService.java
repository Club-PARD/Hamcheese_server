package pard.server.com.longkathon.portfolio.hashtag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class HashtagService {
    private final HashtagRepository hashtagRepository;

    //hashtag 저장
    @Transactional
    public void deleteAndSave(List<String> hashtagList, Long portfolioId) {
        hashtagRepository.deleteAllByPortfolioId(portfolioId);

        List<Hashtag> entityList = hashtagList.stream()
                .map(hashtagName -> Hashtag.builder()
                        .portfolioId(portfolioId)
                        .hashtagName(hashtagName)
                        .build())
                .toList();
        hashtagRepository.saveAll(entityList);
    }


    //해당 포폴에 속한 해시태그 읽기
    public List<String> read(Long portfolioId) {
        List<Hashtag> hashtagList = hashtagRepository.findAllByPortfolioId(portfolioId);
        return hashtagList.stream().map(hashtag -> hashtag.getHashtagName()).collect(Collectors.toList());
    }
}

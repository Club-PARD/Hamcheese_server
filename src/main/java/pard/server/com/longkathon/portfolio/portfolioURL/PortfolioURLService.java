package pard.server.com.longkathon.portfolio.portfolioURL;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pard.server.com.longkathon.portfolio.PortfolioRepository;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioURLService {
    private final PortfolioURLRepository portfolioURLRepository;

    //hashtag 일괄 삭제 및 저장
    @Transactional
    public void deleteAndSave(List<String> urlList, Long portfolioId) {
        portfolioURLRepository.deleteAllByPortfolioId(portfolioId);

        List<PortfolioURL> entityList = urlList.stream()
                .map(url -> PortfolioURL.builder()
                        .portfolioId(portfolioId)
                        .url(url)
                        .build())
                .toList();
        portfolioURLRepository.saveAll(entityList);
    }

    //해당 포폴에 속한 url읽기
    public List<String> read(Long portfolioId) {
        List<PortfolioURL> urlList = portfolioURLRepository.findAllByPortfolioId(portfolioId);
        return urlList.stream().map(url -> url.getUrl()).collect(Collectors.toList());
    }
}

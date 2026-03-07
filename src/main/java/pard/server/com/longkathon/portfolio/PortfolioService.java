package pard.server.com.longkathon.portfolio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import pard.server.com.longkathon.portfolio.portfolioFile.PortfolioFileService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final PortfolioFileService portfolioFileService;

    public List<PortfolioDTO.Res1> getPortfolioInProfile (Long userId) {
        List<Portfolio> portfolioList = portfolioRepository.findAllByUserId(userId);

        return portfolioList.stream().map(portfolio ->
                PortfolioDTO.Res1.builder()
                        .title(portfolio.getTitle())
                        .postDate(portfolio.getCreatedAt())
                        .imageUrl(portfolioFileService.getThumbURL(portfolio.getPortfolioId()))
                        .build())
                .toList();
    }
}

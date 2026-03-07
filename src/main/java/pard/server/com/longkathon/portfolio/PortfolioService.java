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

    public List<PortfolioDTO.Res1> getPortfolioTabPostOrder (Long userId) {
        // 생성일 기준 최신순으로 정렬된 포트폴리오 목록 조회
        List<Portfolio> portfolioList = portfolioRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        return portfolioList.stream().map(portfolio ->
                PortfolioDTO.Res1.builder()
                        .title(portfolio.getTitle())
                        .postDate(portfolio.getCreatedAt())
                        .imageUrl(portfolioFileService.getThumbURL(portfolio.getPortfolioId()))
                        .build())
                .toList();
    }

    public List<PortfolioDTO.Res1> getPortfolioTabRealOrder (Long userId) {
        // 프로젝트 시작일 기준 최신순으로 정렬된 포트폴리오 목록 조회
        List<Portfolio> portfolioList = portfolioRepository.findAllByUserIdOrderByStartDateDesc(userId);

        return portfolioList.stream().map(portfolio ->
                        PortfolioDTO.Res1.builder()
                                .title(portfolio.getTitle())
                                .postDate(portfolio.getCreatedAt())
                                .imageUrl(portfolioFileService.getThumbURL(portfolio.getPortfolioId()))
                                .build())
                .toList();
    }
}

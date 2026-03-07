package pard.server.com.longkathon.portfolio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;

    public List<PortfolioDTO.Res1> getPortfolioInProfile (Long userId) {

    }
}

package pard.server.com.longkathon.portfolio.portfolioURL;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfolioURL {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portfolioURLId;

    private Long portfolioId; // 어느 포폴에 속한 URL

    private String url;
}

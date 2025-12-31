package pard.server.com.longkathon.portfolioLikes;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PortfolioLikes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portfolioLikesId;

    private Long portfolioId; //어떤 포트폴리오에 달린 좋아요인지
}

package pard.server.com.longkathon.likes.likePortfolio;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfolioLike { //좋아요 누른 포트폴리오
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long PortfolioLikeId;

    private Long userId; // 어느 사용자가 누른 좋아요인지

    private Long portfolioId; // 어느 포폴에 달린 좋아요인지

}

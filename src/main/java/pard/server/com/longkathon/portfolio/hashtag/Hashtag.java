package pard.server.com.longkathon.portfolio.hashtag;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Hashtag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hashtagId;

    private Long portfolioId; //어느 포폴에 속한 해시태그인지

    private String hashtagName; // 해시태그 이름
}

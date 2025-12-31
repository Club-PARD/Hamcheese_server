package pard.server.com.longkathon.portfolioFile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfolioFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long PortfolioFileId;

    private String fileName; //파일 이름

    private Long portfolioId; // 해당 파일이 어떤 포폴 게시글에 속하는지

}

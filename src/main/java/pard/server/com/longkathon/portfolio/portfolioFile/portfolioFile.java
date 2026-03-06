package pard.server.com.longkathon.portfolio.portfolioFile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class portfolioFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portfolioFileId;

    private Long portfolioId; // 어느 포폴에 속한 사진인지

    private String fileName; // 사진 파일 이름
}

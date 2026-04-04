package pard.server.com.longkathon.portfolio.portfolioFile;
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
    private Long portfolioFileId;

    private Long portfolioId; // 어느 포폴에 속한 사진인지

    private String fileName; // 사진 파일 이름 (S3에 저장된 UUID 기반 파일명)

    private String originalFileName; // 원본 파일명 (업로드된 파일의 실제 이름)

    private boolean isThumbnail; //썸넬인지 아닌지 여부

    // 썸네일 상태 업데이트 메서드
    public void setThumbnail(boolean isThumbnail) {
        this.isThumbnail = isThumbnail;
    }
}

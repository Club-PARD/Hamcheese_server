package pard.server.com.longkathon.likes.scrapRecruiting;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecruitingScrap { //스크랩한 모집글
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long RecruitingScrapId;

    private Long userId; // 어느 사용자의 스크랩인지

    private Long RecruitingId; // 어느 모집글인지
}

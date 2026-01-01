package pard.server.com.longkathon.MyPage.peerReview;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PeerReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long PeerReviewId;

    String review; //평가문장

    private Long writerId; //동료평가 작성자 id

    private Long userId; //누구에 대한 동료평가인지

    private String meetCategory; //평가대상을 어디서 만났는지

    private String meetSpecific; //구체적으로 어떤 수업인지, 어떤대회인지

    private String startDate; // 프로젝트 시작 기간 (년-월)

    private String endDate;   // 프로젝트 종료 기간 (년-월)
}

package pard.server.com.longkathon.career;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Career {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long careerId;

    private Long userId; //어떤 계정의 경력정보인지

    private String community; //직책

    private String position; //직책

    private String task; //업무

    private String startDate; //활동 시작

    private String endDate; //활동종료

    private String content; //세부 내용
}

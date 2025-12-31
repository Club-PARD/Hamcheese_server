package pard.server.com.longkathon.award;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Award {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long awardId;

    private Long userId; //어떤 계정의 수상정보인지

    private String awardTitle; //수상 경력 이름

    private String awardDepartment; //수상 기구

    private Integer awardYear; //취득 년도
}

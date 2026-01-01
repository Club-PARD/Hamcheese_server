package pard.server.com.longkathon.MyPage.skillStackList;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class skillStackList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long skillStackListId;

    private Long introductionId; //어떤 자기소개에 대한 스택인지

    private String skillName; //스택이름
}

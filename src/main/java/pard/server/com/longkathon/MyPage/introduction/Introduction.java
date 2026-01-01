package pard.server.com.longkathon.MyPage.introduction;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Introduction { // 마이
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long introductionId;

    private Long userId ; //어떤 마이페이지에 속하는지

    private String oneLine; //한줄소개
}

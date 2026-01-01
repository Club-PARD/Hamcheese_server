package pard.server.com.longkathon.MyPage.user;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name; // 사용자 이름
    private String email; //사용자 학년
    private String studentId; //학번
    private String department; //학부
    private String firstMajor; //1전공
    private String secondMajor; //2전공
    private String phoneNumber; //전화번호
    private String gpa; // 학점

    private int grade; //학년
    private int semester; //학기수
}

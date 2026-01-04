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
    private String studentId; //학번
    private String grade; //학년
    private String semester; //학기수
    private String department; //학부
    private String firstMajor; //1전공
    private String secondMajor; //2전공
    private String gpa; // 학점
    private String email; //사용자 학년

    private String socialId;



    public void updateMyprofile (UserDTO.UserRes3 userRes) {
        this.name = userRes.getName();
        this.studentId = userRes.getStudentId();
        this.grade = userRes.getGrade();
        this.semester = userRes.getSemester();
        this.department = userRes.getDepartment();
        this.firstMajor = userRes.getFirstMajor();
        this.secondMajor = userRes.getSecondMajor();
        this.gpa = userRes.getGpa();
        this.email = userRes.getEmail();
    }
}

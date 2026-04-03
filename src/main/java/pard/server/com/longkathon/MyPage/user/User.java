package pard.server.com.longkathon.MyPage.user;
import jakarta.persistence.*;
import lombok.*;
import pard.server.com.longkathon.BaseEntity.BaseEntity;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name; // 사용자 이름
    private Long studentId; //학번
    private String grade; //학년
    private String semester; //학기수
    private String department; //학부
    private String firstMajor; //1전공
    private String secondMajor; //2전공
    private String gpa; // 학점
    private String email; //사용자 학년

    private boolean isProfileCompleted;

    private int points;

    //프로필 수정
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

    //인적사항 첫 입력
    public void completeProfile(UserDTO.UserReq1 userReq) {
        this.name = userReq.getName();
        this.studentId = userReq.getStudentId();
        this.grade = userReq.getGrade();
        this.semester = userReq.getSemester();
        this.department = userReq.getDepartment();
        this.firstMajor = userReq.getFirstMajor();
        this.secondMajor = userReq.getSecondMajor();
        this.gpa = userReq.getGpa();
        this.isProfileCompleted = true;
    }

    public void pointsMinus(int cost){
        points -= cost;
    }

    public void pointsPlus(int cost){
        points += cost;
    }
}

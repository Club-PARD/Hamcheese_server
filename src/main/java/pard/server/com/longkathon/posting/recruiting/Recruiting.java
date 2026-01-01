package pard.server.com.longkathon.posting.recruiting;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Recruiting { //구인 글 포스팅
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recruitingId;

    private Long userId; //누구가 작성한 건지

    private String projectType; //프로젝트 형태 (수업, 졸작, 동아리, 학회, 대회)

    private String projectSpecific;// 구체적인 이름 (수업 이름)

    private int classes;//분반

    private String topic;//주제

    private int totalPeople;//전체인원

    private int recruitPeople;//모집인원

    private String title;//제목

    private String context; //내용


}

package pard.server.com.longkathon.posting.recruiting;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

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

    private String classes;//분반

    private String topic;//주제

    private int totalPeople;//전체인원

    private int recruitPeople;//모집인원

    private String title;//제목

    private String context; //내용

    @Column(updatable = false)
    private LocalDateTime date;

    @PrePersist // 생성 시점으로 자동 설정
    public void prePersist() {
        if (this.date == null) {
            this.date = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                    .truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
        }
    }

    // 모집글 수정
    public void update(RecruitingDTO.RecruitingPatchReq req) {
        if (req.getProjectType() != null) this.projectType = req.getProjectType();
        if (req.getProjectSpecific() != null) this.projectSpecific = req.getProjectSpecific();
        if (req.getClasses() != null) this.classes = req.getClasses();
        if (req.getTopic() != null) this.topic = req.getTopic();
        if (req.getTotalPeople() != null) this.totalPeople = req.getTotalPeople();
        if (req.getRecruitPeople() != null) this.recruitPeople = req.getRecruitPeople();
        if (req.getTitle() != null) this.title = req.getTitle();
        if (req.getContext() != null) this.context = req.getContext();
    }



}

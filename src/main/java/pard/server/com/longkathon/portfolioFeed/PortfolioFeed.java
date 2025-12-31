package pard.server.com.longkathon.portfolioFeed;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfolioFeed { //포트폴리오 피드
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portfolioFeedId;

    private Long userId; //어떤 계정의 포폴 피드인지

    private String title; //피드 제목

    private String field; //어디서 진행했는지, 단체

    private String territory; //영역, 분야

    private String startDate; //해당 프로젝트 시작시점

    private String endDate; //해당 프로젝트 종료시점

    private String detail; //세부 기여항목

    private String context; //포폴을 설명하는 내용
}

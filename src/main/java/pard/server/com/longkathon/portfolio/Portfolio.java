package pard.server.com.longkathon.portfolio;
import jakarta.persistence.*;
import lombok.*;
import pard.server.com.longkathon.BaseEntity.BaseEntity;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Portfolio extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portfolioId;

    private Long userId; //어느 유저의 포폴인지

    private String title; //포폴 제목

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description; //포폴 설명글

    private String startDate; //진행기간의 시작 시점

    private String endDate; //진행 기간의 종료 시점
}

package pard.server.com.longkathon.portfolio;
import jakarta.persistence.*;
import lombok.*;
import pard.server.com.longkathon.BaseEntity.BaseEntity;

import java.time.LocalDateTime;

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

    private String organization; //진행된 단체

    private String category; //기여 분야

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description; //포폴 설명글

    private LocalDateTime startDate; //진행기간의 시작 시점

    private LocalDateTime endDate; //진행 기간의 종료 시점
}

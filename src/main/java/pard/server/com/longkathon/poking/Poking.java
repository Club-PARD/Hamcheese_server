package pard.server.com.longkathon.poking;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Poking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pokingId;

    private Long sendId; // 찌르기를 보낸사람

    private Long receiveId; //찌르기를 받는사람

    private Long recruitingId;

    @Column(updatable = false)
    private LocalDateTime date;

    @PrePersist // 생성 시점으로 자동 설정
    public void prePersist() {
        if (this.date == null) {
            this.date = LocalDateTime.now();
        }
    }

}
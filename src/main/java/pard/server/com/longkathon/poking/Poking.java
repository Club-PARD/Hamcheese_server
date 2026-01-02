package pard.server.com.longkathon.poking;
import jakarta.persistence.*;
import lombok.*;

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
}

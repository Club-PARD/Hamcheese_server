package pard.server.com.longkathon.likes.keepMate;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeepMate { // 찜한 메이트
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long KeepMateId;

    private Long userId; // 어느 사용자의 찜인지

    private Long keepUserId; // 찜 당한 사람이 누구인지
}

package pard.server.com.longkathon.peerBadKeyword;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PeerBadKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long PeerKeywordId;

    private Long userId; //누구에 대한 키워드인지

    private Long writerId; //어떤 사람이 작성한 것인지

    private String keyword; //어떤 키워드인지

    private int count; //해당 키워드
}

package pard.server.com.longkathon.MyPage.peerGoodKeyword;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PeerGoodKeyword { //한 유저에 대한 동료평가 키워드 모음
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long PeerKeywordId;

    private Long userId; //누구에 대한 키워드인지

    private String keyword; //어떤 키워드인지

    private int count; //해당 키워드

    public void increaseCount(int delta) {
        this.count += delta;
    }

}

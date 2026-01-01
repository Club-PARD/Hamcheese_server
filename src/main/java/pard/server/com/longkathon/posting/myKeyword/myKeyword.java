package pard.server.com.longkathon.posting.myKeyword;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class myKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long myKeywordId;

    private Long recruitingId; //어떤 구인글에 속한 키워드인지

    private String keyword; //무슨 키워드인지
}

package pard.server.com.longkathon.selfKeyword;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SelfKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long selfKeywordId;

    private Long profileFeedId; //어떤 프로필 피드에 속하는지

    private String selfKeyword; //키워드 자체
}

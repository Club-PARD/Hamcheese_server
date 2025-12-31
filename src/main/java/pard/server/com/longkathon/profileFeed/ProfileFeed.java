package pard.server.com.longkathon.profileFeed;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileFeed { //자기소개 게시글
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileFeedId;

    private Long userId; //어떤 유저의 프로필 피드인지,
}

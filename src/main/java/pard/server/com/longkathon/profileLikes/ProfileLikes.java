package pard.server.com.longkathon.profileLikes;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ProfileLikes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileLikesId;

    private Long userId; //어떤 계정에 달린 좋아요인지

    private Long writerId; //누가 누른 좋아요인지
}


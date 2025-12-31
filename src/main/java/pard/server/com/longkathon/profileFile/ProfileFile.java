package pard.server.com.longkathon.profileFile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileFile { //프로필 피드에 속하는 이미지 파일
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileFileId;

    private Long profileFeedId; //해당 파일이 어느 프로필 피드 게시글에 속하는지

    private String fileName; //파일 이름을 유지한다.
}

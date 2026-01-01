package pard.server.com.longkathon.MyPage.userFile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class userFile { //프로필에 속하는 이미지 파일
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userFileId;

    private Long userId; //해당 파일이 어느 계정에 속하는지

    private String fileName; //파일 이름을 유지한다.
}

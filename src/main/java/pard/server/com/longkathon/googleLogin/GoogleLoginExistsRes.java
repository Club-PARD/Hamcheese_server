package pard.server.com.longkathon.googleLogin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleLoginExistsRes {
    private boolean exists;   // DB에 있는 유저면 true
    private String email;     // (편의) 프론트에서 재사용 가능
    private String socialId;  // (편의) 프론트에서 재사용 가능
    private Long myId; //앞으로 계속 요청할 로그인된 유저의 ID
    private String name; //이름
    private String imageUrl; //사진주소
}

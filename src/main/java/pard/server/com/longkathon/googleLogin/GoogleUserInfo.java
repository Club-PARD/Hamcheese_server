package pard.server.com.longkathon.googleLogin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleUserInfo {
    private String email;
    private String socialId; // = sub
}

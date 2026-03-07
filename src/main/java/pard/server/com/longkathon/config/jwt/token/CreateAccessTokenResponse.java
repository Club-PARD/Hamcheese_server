package pard.server.com.longkathon.config.jwt.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CreateAccessTokenResponse {
    private String accessToken;
    private boolean isProfileCompleted;
    private String name;          // 사용자 이름
    private String imageUrl;      // 프로필 사진 URL
}

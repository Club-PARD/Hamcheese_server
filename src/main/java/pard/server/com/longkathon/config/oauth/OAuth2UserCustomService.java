package pard.server.com.longkathon.config.oauth;


import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import pard.server.com.longkathon.MyPage.user.User;
import pard.server.com.longkathon.MyPage.user.UserRepo;


import java.util.Map;

@RequiredArgsConstructor
@Service
//“구글 로그인 성공 후, 구글에서 받아온 사용자 정보를 우리 DB의 User 테이블에 저장/업데이트해주는 서비스”
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final UserRepo userRepository;

    @Override //google의 리소스 서버에서 보내주는 사용자 정보를 불러오는 메서드
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest); //Spring이 제공하는 부모클래스 DefaultOAuth2UserService를 통해
        //구글에서 사용자 정보를 받아와서 OAuth2User로 만들어줌

        // 한동대학교 이메일 검증
        //validateHandongEmail(user);

        saveOrUpdate(user);

        return user;
    }

    /**
     * 한동대학교 이메일 도메인 검증
     * @param oAuth2User 구글에서 받아온 사용자 정보
     * @throws OAuth2AuthenticationException 한동대학교 이메일이 아닌 경우
     */
    private void validateHandongEmail(OAuth2User oAuth2User) throws OAuth2AuthenticationException {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");

        // null 체크
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_email"),
                "이메일 정보를 가져올 수 없습니다."
            );
        }

        // 한동대학교 도메인 검증
        if (!email.endsWith("@handong.ac.kr")) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("unauthorized_domain"),
                "한동대학교 계정(@handong.ac.kr)만 가입할 수 있습니다."
            );
        }
    }

    private User saveOrUpdate(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        return userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .isProfileCompleted(false)
                                .point(0)
                                .build()
                ));
    }
}

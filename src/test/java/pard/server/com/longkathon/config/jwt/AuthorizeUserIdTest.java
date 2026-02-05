package pard.server.com.longkathon.config.jwt;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import pard.server.com.longkathon.MyPage.user.AuthorizeUserId;
import pard.server.com.longkathon.MyPage.user.User;
import pard.server.com.longkathon.config.jwt.token.CustomPrincipal;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//임의의 유저를 생성하여 SecurityContextHolder에 넣고 AuthorizeUserIdTest가 잘 작동하는지 테스트
class AuthorizeUserIdRealPrincipalTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAuthorizedUserId_usesRealProjectMethod_andRealPrincipal() throws Exception {
        // given: 실제 User 엔티티 생성
        User user = new User();
        setPrivateField(user, "userId", 12L);
        setPrivateField(user, "email", "daniel@test.com");
        setPrivateField(user, "name", "daniel");

        // ✅ 여기서 "프로젝트의 실제 CustomPrincipal"을 사용
        // 예) record CustomPrincipal(Long userId, String email, ...) 라면:
        // CustomPrincipal principal = new CustomPrincipal(user.getUserId(), user.getEmail(), ...);

        Long userId = (Long) getPrivateField(user, "userId");
        String email = (String) getPrivateField(user, "email");
        CustomPrincipal principal = new CustomPrincipal(userId, email);

        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when: 실제 프로젝트 메서드 호출
        Long actual = AuthorizeUserId.getAuthorizedUserId();

        // then
        assertEquals(12L, actual);
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Object getPrivateField(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(target);
    }
}

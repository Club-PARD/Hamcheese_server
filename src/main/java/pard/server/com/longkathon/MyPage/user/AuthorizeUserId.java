package pard.server.com.longkathon.MyPage.user;

import org.springframework.security.core.context.SecurityContextHolder;
import pard.server.com.longkathon.config.jwt.token.CustomPrincipal;

public class AuthorizeUserId {
    public static Long getAuthorizedUserId(){
        CustomPrincipal p = (CustomPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return p.userId();
    }
}

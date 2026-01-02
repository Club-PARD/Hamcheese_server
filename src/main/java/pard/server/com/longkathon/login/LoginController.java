package pard.server.com.longkathon.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller//페이지 컨트롤러: 아래 주소로 들어오면 loginform 페이지로 보낸다.
public class LoginController {
    @GetMapping("/loginform")
    public String login(){
        return "loginform";
    }
}
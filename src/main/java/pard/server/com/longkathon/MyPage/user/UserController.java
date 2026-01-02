package pard.server.com.longkathon.MyPage.user;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor

public class UserController {
    private final UserService userService;

    @GetMapping("mateProfile/{myId}/{userId}")
    public UserDTO.UserRes1 mateProfile(@PathVariable Long myId, @PathVariable Long userId) {
        return userService.readMateProfile(myId, userId);

    }

}

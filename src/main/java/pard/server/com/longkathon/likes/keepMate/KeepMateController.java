package pard.server.com.longkathon.likes.keepMate;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pard.server.com.longkathon.MyPage.user.UserDTO;

import java.util.List;

@RestController
@RequestMapping("/keepMate")
@RequiredArgsConstructor
public class KeepMateController {
    private final KeepMateService keepMateService;

    /**
     * 메이트 찜하기 토글 (추가/취소)
     * POST /keepMate/{keepUserId}
     */
    @PostMapping("/{keepUserId}")
    public ResponseEntity<KeepMateDTO.Response> toggleKeepMate(@PathVariable Long keepUserId) {
        KeepMateDTO.Response response = keepMateService.toggleKeepMate(keepUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인한 유저가 찜한 메이트 리스트 조회
     * GET /keepMate/findAll
     */
    @GetMapping("/findAll")
    public ResponseEntity<List<UserDTO.UserRes5>> findAll() {
        List<UserDTO.UserRes5> keepMates = keepMateService.findAllKeepMates();
        return ResponseEntity.ok(keepMates);
    }
}


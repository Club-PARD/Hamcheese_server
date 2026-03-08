package pard.server.com.longkathon.poking;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pard.server.com.longkathon.MyPage.user.AuthorizeUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/poking")
public class PokingController {
    private final PokingService pokingService;

    // 찌르기 생성: sendId(보낸 사람), receiveId(받는 사람)
    @PostMapping("/{recruitingId}") //게시글에서 찌르기
    public ResponseEntity<PokingRes.pokingRes1> createPoking(@PathVariable Long recruitingId) {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        return ResponseEntity.ok(pokingService.createPoking(recruitingId, myId));
    }

    @PostMapping("/user/{userId}") //프로필에서 찌르기
    public ResponseEntity<PokingRes.pokingRes1> createToUser(@PathVariable Long userId) {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        return ResponseEntity.ok(pokingService.createPokingToUser(userId, myId));
    }

    //찌르기 가능 여부 확인 (모집글에서)
    @GetMapping("/recruiting/{recruitingId}") // 찌르기가 이미 존재하는지 여부
    public ResponseEntity<PokingRes.CanPokeRes> canPokeRecruiting(@PathVariable Long recruitingId) {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        return ResponseEntity.ok(pokingService.canPokeRecruiting(recruitingId, myId));
    }

    //찌르기 가능 여부 확인 (유저 프로필에서)
    @GetMapping("/userProfile/{userId}") // 찌르기가 이미 존재하는지 여부
    public ResponseEntity<PokingRes.CanPokeRes> canPokeProfile(@PathVariable Long userId) {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        return ResponseEntity.ok(pokingService.canPokeProfile(userId, myId));
    }

    @GetMapping("") //내가 받은 찌르기 목록
    public ResponseEntity<java.util.List<PokingRes.pokingRes2>> received() {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        return ResponseEntity.ok(pokingService.received(myId));
    }

    @DeleteMapping("/{pokingId}") //다음 기회에 버튼
    public ResponseEntity<PokingRes.PokingResponseResult> delete(@PathVariable Long pokingId, @RequestBody PokingReq pokingReq) {
        return ResponseEntity.ok(pokingService.delete(pokingId, pokingReq));
    }
}

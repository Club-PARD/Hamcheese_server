package pard.server.com.longkathon.poking;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/poking")
public class PokingController {
    private final PokingService pokingService;

    //찌르기 가능 여부 확인 (유저 프로필에서)
    @GetMapping("/canInProfile/{userId}/{myId}") // 찌르기가 이미 존재하는지 여부
    public ResponseEntity<PokingRes.CanPokeRes> canPokeProfile(
            @PathVariable Long userId,
            @PathVariable Long myId
    ) {
        return ResponseEntity.ok(pokingService.canPokeProfile(userId, myId));
    }

    //찌르기 가능 여부 확인 (모집글에서)
    @GetMapping("/canInRecruiting/{recruitingId}/{myId}") // 찌르기가 이미 존재하는지 여부
    public ResponseEntity<PokingRes.CanPokeRes> canPokeRecruiting(
            @PathVariable Long recruitingId,
            @PathVariable Long myId
    ) {
        return ResponseEntity.ok(pokingService.canPokeRecruiting(recruitingId, myId));
    }

    // 찌르기 생성: sendId(보낸 사람), receiveId(받는 사람)
    @PostMapping("/{recruitingId}/{myId}")
    public ResponseEntity<PokingRes.pokingRes1> createPoking(
            @PathVariable Long recruitingId,
            @PathVariable Long myId
    ) {
        return ResponseEntity.ok(pokingService.createPoking(recruitingId, myId));
    }

    @GetMapping("/received/{myId}") //내가 받은 찌르기 목록
    public ResponseEntity<java.util.List<PokingRes.pokingRes2>> received(@PathVariable Long myId) {
        return ResponseEntity.ok(pokingService.received(myId));
    }

    @PostMapping("/user/{userId}/{myId}")
    public ResponseEntity<PokingRes.pokingRes1> createToUser(@PathVariable Long userId, @PathVariable Long myId) {
        return ResponseEntity.ok(pokingService.createPokingToUser(userId, myId));
    }

    @DeleteMapping("/{pokingId}")
    public ResponseEntity<Void> delete(@PathVariable Long pokingId) {
        pokingService.delete(pokingId);
        return ResponseEntity.ok().build();
    }


    //찌르기 가능 여부 확인 (모집 글에서)
}

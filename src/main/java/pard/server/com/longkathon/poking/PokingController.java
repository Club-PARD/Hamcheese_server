package pard.server.com.longkathon.poking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/poking")
public class PokingController {
    private final PokingService pokingService;



    // 찌르기 생성: sendId(보낸 사람), receiveId(받는 사람)
    @PostMapping("/{recruitingId}/{myId}")
    public ResponseEntity<PokingRes.pokingRes1> createPoking(
            @PathVariable Long recruitingId,
            @PathVariable Long myId
    ) {
        return ResponseEntity.ok(pokingService.createPoking(recruitingId, myId));
    }
}

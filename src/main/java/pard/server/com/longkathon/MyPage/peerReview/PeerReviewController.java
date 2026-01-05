package pard.server.com.longkathon.MyPage.peerReview;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/peerReview")
@RequiredArgsConstructor
public class PeerReviewController {
    private final PeerReviewService peerReviewService;

    @PostMapping("{myId}/{userId}")
    public ResponseEntity<Void> createPeerReview(@PathVariable Long myId, @PathVariable Long userId, @RequestBody PeerReviewDTO.PeerReviewReq1 peerReviewReq) {
        peerReviewService.createPeerReview(myId, userId, peerReviewReq);
        return ResponseEntity.ok().build();
    }
}

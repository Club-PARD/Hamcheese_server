package pard.server.com.longkathon.MyPage.peerReview;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pard.server.com.longkathon.MyPage.user.AuthorizeUserId;

@RestController
@RequestMapping("/peerReview")
@RequiredArgsConstructor
public class PeerReviewController {
    private final PeerReviewService peerReviewService;

    @PostMapping("/{userId}")
    public ResponseEntity<Void> createPeerReview(@PathVariable Long userId, @RequestBody PeerReviewDTO.PeerReviewReq1 peerReviewReq) {
        Long myId = AuthorizeUserId.getAuthorizedUserId(); //SecurityContextHolder가 유지하는 userId를 가져온다.
        peerReviewService.createPeerReview(myId, userId, peerReviewReq);
        return ResponseEntity.ok().build();
    }
}

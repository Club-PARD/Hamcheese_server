package pard.server.com.longkathon.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks")
public class SendbirdWebhookController {

    private final SendbirdWebhookService sendbirdWebhookService;

    @PostMapping("/sendbird")
    public ResponseEntity<Void> receive(
            @RequestHeader(value = "x-sendbird-signature", required = false) String signature,
            @RequestBody String rawBody
    ) {
        // MVP: 컨트롤러는 최대한 빨리 끝내기
        sendbirdWebhookService.handle(rawBody, signature);
        return ResponseEntity.ok().build();
    }
}

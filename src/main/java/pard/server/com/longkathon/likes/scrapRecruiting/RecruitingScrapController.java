package pard.server.com.longkathon.likes.scrapRecruiting;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pard.server.com.longkathon.posting.recruiting.RecruitingDTO;

import java.util.List;

@RestController
@RequestMapping("/recruitingScrap")
@RequiredArgsConstructor
public class RecruitingScrapController {
    private final RecruitingScrapService recruitingScrapService;

    /**
     * 스크랩 토글 (추가/취소)
     */
    @PostMapping("/{recruitingId}")
    public ResponseEntity<RecruitingScrapDTO.Response> toggleScrap(@PathVariable Long recruitingId) {
        return ResponseEntity.ok(recruitingScrapService.toggleScrap(recruitingId));
    }

    /**
     * 스크랩한 모집글 목록 조회
     */
    @GetMapping("/findAll")
    public ResponseEntity<List<RecruitingDTO.RecruitingRes1>> findAllScrappedRecruitings() {
        return ResponseEntity.ok(recruitingScrapService.findAllScrappedRecruitings());
    }

    /**
     * 특정 모집글의 스크랩 수 조회
     */
    @GetMapping("/count/{recruitingId}")
    public ResponseEntity<Long> getScrapCount(@PathVariable Long recruitingId) {
        return ResponseEntity.ok(recruitingScrapService.getScrapCount(recruitingId));
    }
}

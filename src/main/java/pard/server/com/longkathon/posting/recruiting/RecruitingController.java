package pard.server.com.longkathon.posting.recruiting;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recruiting")
@RequiredArgsConstructor
public class RecruitingController {

    private final RecruitingService recruitingService;

    @GetMapping("/findAll")
    public ResponseEntity<List<RecruitingDTO.RecruitingRes1>> findAll() {
        return ResponseEntity.ok(recruitingService.viewAllRecruiting());
    }

    @GetMapping("/detail/{recruitingId}/{myId}") // 모집글 상세 조회
    public ResponseEntity<RecruitingDTO.RecruitingRes2> findById(
            @PathVariable Long recruitingId,
            @PathVariable Long myId
    ) {
        return ResponseEntity.ok(recruitingService.viewRecruitingDetail(recruitingId, myId));
    }

    @GetMapping("/filter") // 모집글 필터 적용
    public ResponseEntity<List<RecruitingDTO.RecruitingRes3>> filter(
            @RequestParam(name = "type", required = false) List<String> type,
            @RequestParam(name = "departments", required = false) List<String> departments,
            @RequestParam(name = "name", required = false) String name
    ) {
        return ResponseEntity.ok(recruitingService.filter(type, departments, name));
    }


    @GetMapping("/{myId}") // 내 모집글 조회
    public ResponseEntity<List<RecruitingDTO.RecruitingRes4>> viewMyRecruitings(@PathVariable Long myId) {
        return ResponseEntity.ok(recruitingService.viewRecruitingMine(myId));
    }

    @PostMapping("/createPost/{userId}")
    public ResponseEntity<Void> createPost(@PathVariable Long userId, @RequestBody RecruitingDTO.RecruitingReq2 req) {
        recruitingService.createRecruiting(userId, req);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{recruitingId}/{myId}") // 모집글 수정 (부분 수정 PATCH)
    public ResponseEntity<Void> updateRecruiting(
            @PathVariable Long recruitingId,
            @PathVariable Long myId,
            @RequestBody RecruitingDTO.RecruitingPatchReq req
    ) {
        recruitingService.updateRecruiting(recruitingId, myId, req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{recruitingId}/{myId}") // 모집글 삭제
    public ResponseEntity<Void> deleteRecruiting(
            @PathVariable Long recruitingId,
            @PathVariable Long myId
    ) {
        recruitingService.deleteRecruiting(recruitingId, myId);
        return ResponseEntity.ok().build();
    }


}


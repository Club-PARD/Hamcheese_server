package pard.server.com.longkathon.portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pard.server.com.longkathon.MyPage.user.AuthorizeUserId;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/portfolio")
public class PortfolioController { //상세 프로필 페이지, 메인페이지, 마이페이지로 구분
    private final PortfolioService portfolioService;

    //---------------------상세 프로필 페이지----------------------------
    @GetMapping("/detailPagePostOrder/{userId}") // 상세페이지 포트폴리오 탭 리턴 (등록최신순)
    public ResponseEntity<List<PortfolioDTO.Res1>> getPostOrder(@PathVariable Long userId) {
        return ResponseEntity.ok(portfolioService.getPortfolioTabPostOrder(userId));
    }

    @GetMapping("/detailPageRealOrder/{userId}") // 상세페이지 포트폴리오 탭 리턴 (실제 프로젝트 시간 순)
    public ResponseEntity<List<PortfolioDTO.Res1>> getRealOrder(@PathVariable Long userId) {
        return ResponseEntity.ok(portfolioService.getPortfolioTabRealOrder(userId));
    }

    //--------------------- 메인 페이지 ------------------------------------
    @GetMapping("/filter") //메인 페이지의 포트폴리오 기본 get요청도 filter로 통합
    public ResponseEntity<List<PortfolioDTO.Res1>> filter(
            @RequestParam(name = "departments", required = false) List <String> departments,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "firstStudentId", required = false) Long firstStudentId,
            @RequestParam(name = "secondStudentId", required = false) Long secondStudentId,
            @RequestParam(name = "ordering", required = false) String ordering
    ) {
        List<PortfolioDTO.Res1> result = portfolioService.filter(
                departments, name, firstStudentId, secondStudentId, ordering);
        return ResponseEntity.ok(result);
    }

    //--------------------마이 페이지 ----------------------------------

    //게시글 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> create(
            @RequestPart("request") PortfolioDTO.Req1 requestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        Long userId = AuthorizeUserId.getAuthorizedUserId();
        portfolioService.create(requestDTO, images, userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(value = "/{portfolioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> update(
            @PathVariable Long portfolioId,
            @RequestPart("request") PortfolioDTO.Req1 requestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ){
        portfolioService.update(portfolioId, requestDTO, images);
        return ResponseEntity.ok().build();
    }

    //------------------ 모든 페이지 공통 ------------------------------

    @GetMapping("/detail/{portfolioId}") //포폴 상세 정보
    public ResponseEntity<PortfolioDTO.Res2> detail(@PathVariable Long portfolioId) {
        PortfolioDTO.Res2 response = portfolioService.detail(portfolioId);
        return ResponseEntity.ok(response);
    }


}

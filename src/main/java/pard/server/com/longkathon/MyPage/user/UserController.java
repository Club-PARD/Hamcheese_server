package pard.server.com.longkathon.MyPage.user;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pard.server.com.longkathon.MyPage.userFile.UserFileService;
import pard.server.com.longkathon.likes.keepMate.KeepMateService;
import pard.server.com.longkathon.portfolio.PortfolioDTO;
import pard.server.com.longkathon.portfolio.PortfolioService;
import pard.server.com.longkathon.s3.AwsS3Service;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor

public class UserController {
    private final UserService userService;
    private final AwsS3Service awsS3Service;
    private final UserFileService userFileService;
    private final KeepMateService keepMateService;
    private final PortfolioService portfolioService;

    //회원가입에서 인적사항 입력
    @PatchMapping(value="/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO.UserRes2> createUser(
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart("data") String dataJson
    ) throws Exception {
        UserDTO.UserReq1 userReq =
                new ObjectMapper().readValue(dataJson, UserDTO.UserReq1.class);

        String fileName = null; //사진을 안올릴것을 대비하여 일단 null처리

        // 파일이 "존재하고", "비어있지 않을 때만" 업로드
        if (profileImage != null && !profileImage.isEmpty()) {
            fileName = awsS3Service.uploadFile(profileImage); //s3에 업로드
        }
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        UserDTO.UserRes2 result = userService.createUser(userReq, fileName, myId);
        return ResponseEntity.ok(result);
    }

//--------------------------둘러보기 페이지----------------------------------------

    @GetMapping("/defaultOrder") //메이트 둘러보기 페이지에서 모든 프로필 게시물 띄우기
    public ResponseEntity<List<UserDTO.UserRes5>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/popularOrder") // 인기순핕터. 찜 많이 받은 유저 순.
    public ResponseEntity<List<UserDTO.UserRes5>> findPopular() {
        return ResponseEntity.ok(userService.popularOrder());
    }

    @GetMapping("/highStudentIdOrder") // 고학번 순 필터
    public ResponseEntity<List<UserDTO.UserRes5>> findHighest() {
        return ResponseEntity.ok(userService.findHighest());
    }

    @GetMapping("/lowStudentIdOrder") // 저학번 순 필터
    public ResponseEntity<List<UserDTO.UserRes5>> findLowest() {
        return ResponseEntity.ok(userService.findLowest());
    }

    @GetMapping("/filter") // 예: /user/filter?departments=컴공,전자&name=길동
    public ResponseEntity<List<UserDTO.UserRes5>> filter(
            @RequestParam(name = "departments", required = false) List<String> departments,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "firstStudentId", required = false) Long firstStudentId,
            @RequestParam(name = "secondStudentId", required = false) Long secondStudentId
    ) {
        return ResponseEntity.ok(userService.filter(departments, name, firstStudentId, secondStudentId));
    }

    @PostMapping("/keepMate")// 사용자 찜하기, 찜 당한 유저 id를 받음
    public ResponseEntity<Void> keepMate(@PathVariable Long userId) {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        keepMateService.createKeepMate(myId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/keepMate") // 찜한 사용자 삭제
    public ResponseEntity<Void> keepMateDelete(@PathVariable Long userId) {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        keepMateService.deleteKeepMate(myId, userId);
        return ResponseEntity.noContent().build();
    }
//--------------------------------상세 프로필 페이지------------------------------------
    @GetMapping("/equal/{userId}") //프로필 게시글 클릭 시 본인 것인지 유무확인
    public ResponseEntity<Boolean> equal(@PathVariable String userId) {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        if (myId.equals(userId)) {
            return ResponseEntity.ok(true);
        }else{
            return ResponseEntity.ok(false);
        }
    }

    //클릭한 게시물이 남의 게시물이면 (이제 이게 상페프로필 디폴트 값 - 자기소개 탭)
    @GetMapping("/introduction/{userId}") // 상세프로필 자기소개 탭 리턴
    public ResponseEntity<UserDTO.UserRes1> getIntroductionTab(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.readMateProfile(userId));
    }

    @GetMapping("/portfolioPostOrder/{userId}") // 상세프로필 포트폴리오 탭 리턴 (등록최신순)
    public ResponseEntity<List<PortfolioDTO.Res1>> getPortfolioTabPostOrder(@PathVariable Long userId) {
        return ResponseEntity.ok(portfolioService.getPortfolioTabPostOrder(userId));
    }

    @GetMapping("/portfolioRealOrder/{userId}") // 상세프로필 포트폴리오 탭 리턴 (실제 프로젝트 시간 순)
    public ResponseEntity<List<PortfolioDTO.Res1>> getPortfolioTabRealOrder(@PathVariable Long userId) {
        return ResponseEntity.ok(portfolioService.getPortfolioTabRealOrder(userId));
    }

    /*클릭한 게시물이 본인의 게시물이면
    @GetMapping("/myProfile") //마이 페이지 리턴
    public UserDTO.UserRes3 myProfile() {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        return userService.readMyProfile(myId);
    }*/

//-------------------------마이 페이지---------------------------------------

    @DeleteMapping("/myProfile") //프로필 사진 삭제
    public ResponseEntity<Void> deleteMyProfile() {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        userFileService.deleteImageFile(myId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/updateImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateImage(
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        String fileName = null;
        // ✅ 파일이 있을 때만 삭제/업로드 실행
        if (profileImage != null && !profileImage.isEmpty()) {
            userFileService.updateImageFile(myId);      // 기존 사진 DB, aws에서 삭제(있다면)
            fileName = awsS3Service.uploadFile(profileImage); // 새 사진 aws 업로드
            userFileService.createImageFile(myId, fileName); //db에 파일 이름 유지
        }
        return ResponseEntity.ok().build();
    }
    //내 프로필 수정
    @PatchMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> update(@RequestBody UserDTO.UserRes3 userReq) {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        userService.updateMyprofile(myId, userReq);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/myPeerReview") //내 동료평가 탭에 띄울 동료평가들을 가져온다
    public ResponseEntity<UserDTO.UserRes4> myPeerReview() {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        UserDTO.UserRes4 result = userService.myPeerReview(myId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/firstPage")//첫 서비스 소개글 페이지에 띄울 profileFeedList,recruitingFeedList
    public ResponseEntity<UserDTO.UserRes6> firstPage() {
        UserDTO.UserRes6 result = userService.firstPage();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/tokenTest") //테스트 코드
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Test!!!");
    }
}

package pard.server.com.longkathon.MyPage.user;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pard.server.com.longkathon.MyPage.userFile.UserFileService;
import pard.server.com.longkathon.s3.AwsS3Service;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor

public class UserController {
    private final UserService userService;
    private final AwsS3Service awsS3Service;
    private final UserFileService userFileService;

    @GetMapping("equal/{myId}/{userId}") //프로필 게시글 클릭 시 본인 것인지 유무확인
    public boolean equal(@PathVariable String myId, @PathVariable String userId) {
        if (myId.equals(userId)) {
            return true;
        }else{
            return false;
        }
    }

    //클릭한 게시물이 남의 게시물이면
    @GetMapping("mateProfile/{userId}") //메이트 프로필 페이지 리턴
    public UserDTO.UserRes1 mateProfile(@PathVariable Long userId) {
        return userService.readMateProfile(userId);
    }

    //클릭한 게시물이 본인의 게시물이면
    @GetMapping("myProfile/{myId}") //마이 페이지 리턴
    public UserDTO.UserRes3 myProfile(@PathVariable Long myId) {
        return userService.readMyProfile(myId);
    }

    @DeleteMapping("myProfile/{myId}")
    public void deleteMyProfile(@PathVariable Long myId) {
        userFileService.deleteImageFile(myId);
    }

    @PostMapping(value = "/updateImage/{myId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void updateImage(
            @PathVariable Long myId,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        String fileName = null;
        // ✅ 파일이 있을 때만 삭제/업로드 실행
        if (profileImage != null && !profileImage.isEmpty()) {
            userFileService.updateImageFile(myId);      // 기존 사진 DB, aws에서 삭제(있다면)
            fileName = awsS3Service.uploadFile(profileImage); // 새 사진 aws 업로드
            userFileService.createImageFile(myId, fileName); //db에 파일 이름 유지
        }
    }


    //내 프로필 수정
    @PatchMapping(value = "/update/{myId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> update(
            @PathVariable Long myId,
            @RequestBody UserDTO.UserRes3 userReq
    ) {
        userService.updateMyprofile(myId, userReq);
        return ResponseEntity.ok().build();
    }



    @GetMapping("myPeerReview/{myId}") //내 동료평가 탭에 띄울 동료평가들을 가져온다
    public UserDTO.UserRes4 myPeerReview(@PathVariable Long myId) {
        return userService.myPeerReview(myId);
    }

    //유저 생성
    @PostMapping(value="/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDTO.UserRes2 createUser(
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

        return userService.createUser(userReq, fileName);
    }

    @GetMapping("findAll") //메이트 둘러보기 페이지에서 모든 프로필 게시물 띄우기
    public List<UserDTO.UserRes5> findAll() {
        return userService.findAll();
    }

    @GetMapping("/filter") // 예: /user/filter?departments=컴공,전자&name=길동
    public ResponseEntity<List<UserDTO.UserRes5>> filter(
            @RequestParam(name = "departments", required = false) List<String> departments,
            @RequestParam(name = "name", required = false) String name
    ) {
        return ResponseEntity.ok(userService.filter(departments, name));
    }

    @GetMapping("firstPage")//첫 서비스 소개글 페이지에 띄울 profileFeedList,recruitingFeedList
    public UserDTO.UserRes6 firstPage() {
        return userService.firstPage();
    }



}

package pard.server.com.longkathon.MyPage.user;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pard.server.com.longkathon.MyPage.introduction.IntroductionService;
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

    //내 프로필 수정
    @PatchMapping(value = "/update/{myId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void update(
            @PathVariable Long myId,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart("data") String dataJson
    ) throws Exception {
        UserDTO.UserRes3 userReq =
                new ObjectMapper().readValue(dataJson, UserDTO.UserRes3.class);

        String fileName = null;

        // ✅ 파일이 있을 때만 삭제/업로드 실행
        if (profileImage != null && !profileImage.isEmpty()) {
            userFileService.updateImageFile(myId);      // 기존 사진 DB, aws에서 삭제(있다면)
            fileName = awsS3Service.uploadFile(profileImage); // 새 사진 업로드
        }

        // ✅ fileName이 null이면 서비스에서 "이미지 변경 없음"으로 처리
        userService.updateMyprofile(myId, userReq, fileName);
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
            fileName = awsS3Service.uploadFile(profileImage);
        }

        return userService.createUser(userReq, fileName);
    }

    @GetMapping("findAll") //메이트 둘러보기 페이지에서 모든 프로필 게시물 띄우기
    public List<UserDTO.UserRes5> findAll() {
        return userService.findAll();
    }

    /*@GetMapping("filter")
    public List<UserDTO.UserRes5> filter(@RequestBody UserDTO.UserReq2 userReq) {
        return
    }*/

}

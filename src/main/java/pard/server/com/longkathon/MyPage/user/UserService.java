package pard.server.com.longkathon.MyPage.user;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pard.server.com.longkathon.MyPage.activity.ActivityDTO;
import pard.server.com.longkathon.MyPage.activity.ActivityService;
import pard.server.com.longkathon.MyPage.introduction.IntroductionRepo;
import pard.server.com.longkathon.MyPage.introduction.IntroductionService;
import pard.server.com.longkathon.MyPage.peerReview.PeerReviewService;
import pard.server.com.longkathon.MyPage.skillStackList.SkillStackListRepo;
import pard.server.com.longkathon.MyPage.skillStackList.SkillStackListService;
import pard.server.com.longkathon.MyPage.userFile.UserFileService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final IntroductionService introductionService;
    private final SkillStackListService skillStackListService;
    private final ActivityService activityService;
    private final PeerReviewService peerReviewService;
    private final UserFileService userFileService;
    private final IntroductionRepo introductionRepo;
    private final SkillStackListRepo skillStackListRepo;

    public UserDTO.UserRes2 createUser(UserDTO.UserReq1 userReq, String fileName) { //회원가입에서 받은 인적사항으로 유저를 생성
        User user = User.builder() //유저를 생성 후 DB저장
                .name(userReq.getName())
                .grade(userReq.getGrade())
                .studentId(userReq.getStudentId())
                .department(userReq.getDepartment())
                .firstMajor(userReq.getFirstMajor())
                .secondMajor(userReq.getSecondMajor())
                .email(userReq.getEmail())
                .gpa(userReq.getGpa())
                .socialId(userReq.getSocialId())
                .grade(userReq.getGrade())
                .semester(userReq.getSemester())
                .build();
        userRepo.save(user); //DB저장

        if(fileName != null) { //파일이 null이 아닐때만 파일 이름을 DB에 유지
            userFileService.createImageFile(user.getUserId(), fileName); //유저에 대한 프로필사진을 유지하기위함
        }
        UserDTO.UserRes2 userRes = UserDTO.UserRes2.builder() //프론트에게 userId, name을 리턴
                .myId(user.getUserId())
                .name(user.getName())
                .imageUrl(userFileService.getURL(user.getUserId()))
                .build();
        log.info("[createUser] response dto => myId={}, name={}, imageUrl={}",
                userRes.getMyId(), userRes.getName(), userRes.getImageUrl());
        return userRes;
    }

    public UserDTO.UserRes1 readMateProfile(Long userId) {

        User user = userRepo.findById(userId).orElse(null);

        // ✅ activity 먼저 조회해서 로그 찍기
        List<ActivityDTO> activityList = activityService.read(userId);
        log.info("[readMateProfile] userId={}, activityList=null? {}, size={}",
                userId,
                activityList == null,
                activityList == null ? null : activityList.size()
        );

        // 샘플 1개도 확인(있으면)
        if (activityList != null && !activityList.isEmpty()) {
            ActivityDTO first = activityList.get(0);
            log.info("[readMateProfile] first activity => year={}, title={}, link={}",
                    first.getYear(), first.getTitle(), first.getLink()
            );
        }

        UserDTO.UserRes1 res = UserDTO.UserRes1.builder()
                // 1. 프로필관리 섹션
                .name(user.getName())
                .email(user.getEmail())
                .department(user.getDepartment())
                .firstMajor(user.getFirstMajor())
                .secondMajor(user.getSecondMajor())
                .gpa(user.getGpa())
                .studentId(user.getStudentId())
                .semester(user.getSemester())
                .grade(user.getGrade())
                .imageUrl(userFileService.getURL(userId))

                // 2. 자기소개 섹션
                .introduction(introductionService.read(userId))
                .skillList(skillStackListService.read(userId))

                // 3. 활동내역 섹션 (✅ 변수 사용)
                .activity(activityList)

                // 4. 동료평가 섹션
                .peerGoodKeyword(peerReviewService.goodKeyword(userId))
                .goodKeywordCount(peerReviewService.goodKeywordCount(userId))
                .peerBadKeyword(peerReviewService.BadKeyword(userId))
                .badKeywordCount(peerReviewService.badKeywordCount(userId))
                .build();

        // ✅ DTO에 실제로 들어갔는지도 확인
        log.info("[readMateProfile] res.activity=null? {}, size={}",
                res.getActivity() == null,
                res.getActivity() == null ? null : res.getActivity().size()
        );

        return res;
    }

    public UserDTO.UserRes3 readMyProfile(Long userId) { //마이페이지에 띄울정보
        User user = userRepo.findById(userId).orElse(null);
        return UserDTO.UserRes3.builder()
                //1. 프로필관리 섹션
                .name(user.getName())
                .email(user.getEmail())
                .department(user.getDepartment())
                .firstMajor(user.getFirstMajor())
                .secondMajor(user.getSecondMajor())
                .gpa(user.getGpa())
                .studentId(user.getStudentId())
                .semester(user.getSemester())
                .grade(user.getGrade())
                .imageUrl(userFileService.getURL(userId))

                //2. 자기소개 섹션
                .introduction(introductionService.read(userId))
                .skillList(skillStackListService.read(userId))

                //3. 활동내역 섹션
                .activity(activityService.read(userId))
                .build();
    }

    @Transactional
    public void updateMyprofile(Long userId, UserDTO.UserRes3 userReq, String fileName) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 인적사항 수정
        user.updateMyprofile(userReq);

        // 기타 수정
        introductionService.createOrUpdate(userId, userReq.getIntroduction());
        skillStackListService.deleteAndCreate(userId, userReq.getSkillList());
        activityService.deleteAndCreate(userId, userReq.getActivity());

        // ✅ 이미지: fileName이 있을 때만 업데이트 (null이면 아무것도 안 해서 기존 유지)
        if (fileName != null && !fileName.isBlank()) {
            // 네 프로젝트에서 이미지 정보를 어디에 저장하는지에 맞춰 호출하면 됨

            // (예시 1) user 엔티티에 fileName 컬럼d이 있다면
            // user.setProfileImage(fileName);

            // (예시 2) UserFile 테이블로 관리한다면 (추천)
            userFileService.createImageFile(userId, fileName);
            // 또는 기존 로직이 "기존 삭제 후 생성"이라면
            // userFileService.updateImageFile(userId);
            // userFileService.create(userId, fileName);
        }
    }

    public UserDTO.UserRes4 myPeerReview(Long userId) { // 마이페이지에서 동료평가 탭
        return UserDTO.UserRes4.builder()
                .peerGoodKeyword(peerReviewService.goodKeyword(userId))
                .goodKeywordCount(peerReviewService.goodKeywordCount(userId))
                .peerBadKeyword(peerReviewService.BadKeyword(userId))
                .badKeywordCount(peerReviewService.badKeywordCount(userId))
                .build();
    }

    public List<UserDTO.UserRes5> findAll(){ //메이트 둘러보기 탭에 띄울 모든 자기소개 게시물들
        List<User> users = userRepo.findAll();

        return users.stream().map(user ->
                UserDTO.UserRes5.builder()
                        .userId(user.getUserId())
                        .name(user.getName())
                        .firstMajor(user.getFirstMajor())
                        .secondMajor(user.getSecondMajor())
                        .studentId(user.getStudentId())
                        .introduction(introductionService.read(user.getUserId()))
                        .skillList(skillStackListService.read(user.getUserId()))
                        .peerGoodKeywords(peerReviewService.goodKeywordTop3(user.getUserId()))
                        .imageUrl(userFileService.getURL(user.getUserId()))
                        .build()).toList();

    }

    /*public List<UserDTO.UserRes5> filter(UserDTO.UserReq2 userReq){
        List<String> depts = userReq.getDepartment(); // List<String>
        String name = userReq.getName();

        boolean deptEmpty = (depts == null || depts.isEmpty());
        boolean nameEmpty = (name == null || name.isBlank());
    }*/
}

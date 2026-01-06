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
import pard.server.com.longkathon.posting.myKeyword.MyKeywordService;
import pard.server.com.longkathon.posting.recruiting.Recruiting;
import pard.server.com.longkathon.posting.recruiting.RecruitingDTO;
import pard.server.com.longkathon.posting.recruiting.RecruitingRepo;

import java.util.List;
import java.util.Map;
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
    private final RecruitingRepo recruitingRepo;
    private final MyKeywordService myKeywordService;

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

                // 4. 동료평가 섹션 (좋아요 많은순)
                .peerGoodKeyword(peerReviewService.goodKeyword(userId))
                .goodKeywordCount(peerReviewService.goodKeywordCount(userId))
                .peerBadKeyword(peerReviewService.BadKeyword(userId))
                .badKeywordCount(peerReviewService.badKeywordCount(userId))

                // 5. 동료평가 섹션 (최신순)
                .peerReviewRecent(peerReviewService.readRecentPeerReview(userId))
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
    public void updateMyprofile(Long userId, UserDTO.UserRes3 userReq) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 인적사항 수정
        user.updateMyprofile(userReq);

        // 기타 수정
        introductionService.createOrUpdate(userId, userReq.getIntroduction());
        skillStackListService.deleteAndCreate(userId, userReq.getSkillList());
        activityService.deleteAndCreate(userId, userReq.getActivity());
    }

    public UserDTO.UserRes4 myPeerReview(Long userId) { // 마이페이지에서 동료평가 탭
        return UserDTO.UserRes4.builder()
                .peerGoodKeyword(peerReviewService.goodKeyword(userId))
                .goodKeywordCount(peerReviewService.goodKeywordCount(userId))
                .peerBadKeyword(peerReviewService.BadKeyword(userId))
                .badKeywordCount(peerReviewService.badKeywordCount(userId))
                .peerReviewRecent(peerReviewService.readRecentPeerReview(userId))
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

    public UserDTO.UserRes6 firstPage(){
        List<User> users = userRepo.findRandom3();
        List<Recruiting> recruitings = recruitingRepo.findRandom3();

        List<UserDTO.UserRes5> profileFeedList = users.stream()
                .map(u -> UserDTO.UserRes5.builder()
                        .userId(u.getUserId())
                        .name(u.getName())
                        .firstMajor(u.getFirstMajor())
                        .secondMajor(u.getSecondMajor())
                        .studentId(u.getStudentId())
                        .introduction(introductionService.read(u.getUserId()))
                        .skillList(skillStackListService.read(u.getUserId()))
                        .peerGoodKeywords(peerReviewService.goodKeywordTop3(u.getUserId()))
                        .imageUrl(userFileService.getURL(u.getUserId()))
                        .build()).toList();

        List<RecruitingDTO.RecruitingRes5> recruitingFeedList = recruitings.stream()
                .map(r -> RecruitingDTO.RecruitingRes5.builder()

                        .recruitingId(r.getRecruitingId())
                        .name(
                                userRepo.findById(r.getUserId())
                                        .orElseThrow(() -> new IllegalArgumentException("User not found: " + r.getUserId()))
                                        .getName()
                        )
                        .projectType(r.getProjectType())
                        .projectSpecific(r.getProjectSpecific())
                        .classes(r.getClasses())
                        .topic(r.getTopic())
                        .totalPeople(r.getTotalPeople())
                        .recruitPeople(r.getRecruitPeople())
                        .title(r.getTitle())
                        .build()).toList();

        return UserDTO.UserRes6.builder()
                .profileFeedList(profileFeedList)
                .recruitingFeedList(recruitingFeedList)
                .build();
    }

    @Transactional
    public List<UserDTO.UserRes5> filter(List<String> department, String name) {

        boolean hasDept = department != null && !department.isEmpty();
        boolean hasName = name != null && !name.isBlank();

        List<User> users;

        // 1) department + name 둘 다 있을 때
        if (hasDept && hasName) {
            users = userRepo.findByDepartmentInAndNameContaining(department, name);

            // 2) department만 있을 때
        } else if (hasDept) {
            users = userRepo.findByDepartmentIn(department);

            // 3) name만 있을 때
        } else if (hasName) {
            users = userRepo.findByNameContaining(name);

            // 4) 둘 다 없을 때(필터 없음) -> 전체 or 최신순 등 너 정책대로
        } else {
            users = userRepo.findAll(); // 또는 findAllByOrderByUserIdDesc()
        }

        if (users.isEmpty()) return List.of();

        // 2) User -> UserRes5 변환
        return users.stream()
                .map(u -> {
                    List<String> skills = skillStackListService.read(u.getUserId()); // 너가 갖고 있던 skillList 조회
                    Map<String, Integer> goodKeywords = peerReviewService.goodKeywordTop3(u.getUserId()); // top3면 top3
                    String imageUrl = userFileService.getURL(u.getUserId());

                    return UserDTO.UserRes5.builder()
                            .userId(u.getUserId())
                            .name(u.getName())
                            .firstMajor(u.getFirstMajor())
                            .secondMajor(u.getSecondMajor())
                            .studentId(u.getStudentId())
                            .introduction(introductionService.read(u.getUserId())) // 만약 User 엔티티에 없다면 introductionService.get...
                            .skillList(skills)
                            .peerGoodKeywords(goodKeywords)
                            .imageUrl(imageUrl)
                            .build();
                })
                .toList();
    }
}

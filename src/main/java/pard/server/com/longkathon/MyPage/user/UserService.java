package pard.server.com.longkathon.MyPage.user;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pard.server.com.longkathon.MyPage.activity.ActivityService;
import pard.server.com.longkathon.MyPage.introduction.IntroductionService;
import pard.server.com.longkathon.MyPage.peerReview.PeerReviewService;
import pard.server.com.longkathon.MyPage.skillStackList.SkillStackListService;
import pard.server.com.longkathon.MyPage.userFile.UserFileService;
import pard.server.com.longkathon.poking.PokingService;

import java.util.Objects;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final IntroductionService introductionService;
    private final SkillStackListService skillStackListService;
    private final ActivityService activityService;
    private final PeerReviewService peerReviewService;
    private final PokingService pokingService;
    private final UserFileService userFileService;

    public UserDTO.UserRes1 readMateProfile(Long myId, Long userId) {
        User user = userRepo.findById(userId).orElse(null);

        return UserDTO.UserRes1.builder()
                //1. 프로필관리 섹션
                .name(user.getName())
                .email(user.getEmail())
                .department(user.getDepartment())
                .firstMajor(user.getFirstMajor())
                .secondMajor(user.getSecondMajor())
                .gpa(user.getGpa())
                .studentId(user.getStudentId())
                .semester(user.getSemester())
                .imageUrl(userFileService.getURL(userId))

                //2. 자기소개 섹션
                .introduction(introductionService.read(userId))
                .skillList(skillStackListService.read(userId))

                //3. 활동내역 섹션
                .activity(activityService.read(userId))

                //4. 동료평가 섹션
                .peerGoodKeyword(peerReviewService.GoodKeyword(userId))
                .goodKeywordCount(peerReviewService.goodKeywordCount(userId))
                .peerBadKeyword(peerReviewService.BadKeyword(userId))
                .badKeywordCount(peerReviewService.badKeywordCount(userId))

                //5. 찌르기
                .poking(pokingService.getPoking(userId))

                //6. 동일계정인지 판단
                .canEdit(Objects.equals(myId, userId))
                .build();
    }
}

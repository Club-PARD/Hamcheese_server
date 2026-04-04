package pard.server.com.longkathon.likes.keepMate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pard.server.com.longkathon.MyPage.user.AuthorizeUserId;
import pard.server.com.longkathon.MyPage.user.UserDTO;
import pard.server.com.longkathon.MyPage.user.UserRepo;
import pard.server.com.longkathon.common.exception.DuplicateLikeException;
import pard.server.com.longkathon.common.exception.LikeNotFoundException;
import pard.server.com.longkathon.common.exception.UserNotFoundException;
import pard.server.com.longkathon.MyPage.introduction.IntroductionService;
import pard.server.com.longkathon.MyPage.skillStackList.SkillStackListService;
import pard.server.com.longkathon.MyPage.peerReview.PeerReviewService;
import pard.server.com.longkathon.MyPage.userFile.UserFileService;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KeepMateService {
    private final KeepMateRepository keepMateRepository;
    private final UserRepo userRepo;
    private final IntroductionService introductionService;
    private final SkillStackListService skillStackListService;
    private final PeerReviewService peerReviewService;
    private final UserFileService userFileService;

    /**
     * 메이트 찜하기 생성
     */
    @Transactional
    public KeepMateDTO.Response createKeepMate(Long keepUserId) {
        // 1. 로그인된 사용자 ID 획득
        Long userId = AuthorizeUserId.getAuthorizedUserId();

        // 2. 찜하려는 사용자 존재 여부 확인
        if (!userRepo.existsById(keepUserId)) {
            throw new UserNotFoundException("찜하려는 사용자를 찾을 수 없습니다. ID: " + keepUserId);
        }

        // 3. 중복 확인
        if (keepMateRepository.existsByUserIdAndKeepUserId(userId, keepUserId)) {
            throw new DuplicateLikeException("이미 찜한 메이트입니다.");
        }

        // 4. 찜하기 생성
        KeepMate keepMate = KeepMate.builder()
                .userId(userId)
                .keepUserId(keepUserId)
                .build();
        keepMateRepository.save(keepMate);

        // 5. 응답 생성
        return KeepMateDTO.Response.builder()
                .keepUserId(keepUserId)
                .isKeepMate(true)
                .message("메이트 찜하기가 추가되었습니다.")
                .build();
    }

    /**
     * 메이트 찜하기 삭제
     */
    @Transactional
    public KeepMateDTO.Response deleteKeepMate(Long keepUserId) {
        // 1. 로그인된 사용자 ID 획득
        Long userId = AuthorizeUserId.getAuthorizedUserId();

        // 2. 존재 여부 확인
        if (!keepMateRepository.existsByUserIdAndKeepUserId(userId, keepUserId)) {
            throw new LikeNotFoundException("찜하기 기록을 찾을 수 없습니다.");
        }

        // 3. 찜하기 삭제
        keepMateRepository.deleteByUserIdAndKeepUserId(userId, keepUserId);

        // 4. 응답 생성
        return KeepMateDTO.Response.builder()
                .keepUserId(keepUserId)
                .isKeepMate(false)
                .message("메이트 찜하기가 취소되었습니다.")
                .build();
    }

    /**
     * 찜받은 횟수가 많은 순서대로 사용자 ID 리스트 조회
     */
    public List<Long> findMostPopularUserId() {
        return keepMateRepository.findMostPopularUserId();
    }

    /**
     * 현재 로그인된 사용자가 찜한 메이트 리스트 조회
     */
    public List<UserDTO.UserRes5> findAllKeepMates() {
        // 1. 로그인된 사용자 ID 획득
        Long userId = AuthorizeUserId.getAuthorizedUserId();

        // 2. 찜한 유저 ID 목록 조회 (최신순)
        List<Long> keepUserIds = keepMateRepository.findKeepUserIdsByUserId(userId);

        // 3. 각 유저 정보를 UserRes5로 변환
        return keepUserIds.stream()
                .map(keepUserId -> userRepo.findById(keepUserId).orElse(null))
                .filter(Objects::nonNull) // 존재하지 않는 User는 필터링
                .map(user -> UserDTO.UserRes5.builder()
                        .userId(user.getUserId())
                        .name(user.getName())
                        .firstMajor(user.getFirstMajor())
                        .secondMajor(user.getSecondMajor())
                        .studentId(user.getStudentId())
                        .introduction(introductionService.read(user.getUserId()))
                        .skillList(skillStackListService.read(user.getUserId()))
                        .peerGoodKeywords(peerReviewService.goodKeywordTop3(user.getUserId()))
                        .goodKeywordCount(peerReviewService.goodKeywordCount(user.getUserId()))
                        .imageUrl(userFileService.getURL(user.getUserId()))
                        .isKeepMate(true) // 찜한 리스트이므로 모두 true
                        .build())
                .toList();
    }

    /**
     * 현재 사용자의 찜하기 상태 확인
     */
    public boolean isKeptByCurrentUser(Long keepUserId) {
        Long userId = AuthorizeUserId.getAuthorizedUserId();
        return keepMateRepository.existsByUserIdAndKeepUserId(userId, keepUserId);
    }
}


package pard.server.com.longkathon.likes.scrapRecruiting;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pard.server.com.longkathon.MyPage.user.AuthorizeUserId;
import pard.server.com.longkathon.MyPage.user.User;
import pard.server.com.longkathon.MyPage.user.UserRepo;
import pard.server.com.longkathon.posting.myKeyword.MyKeyword;
import pard.server.com.longkathon.posting.myKeyword.MyKeywordRepo;
import pard.server.com.longkathon.posting.recruiting.Recruiting;
import pard.server.com.longkathon.posting.recruiting.RecruitingDTO;
import pard.server.com.longkathon.posting.recruiting.RecruitingRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitingScrapService {
    private final RecruitingScrapRepository recruitingScrapRepository;
    private final RecruitingRepo recruitingRepo;
    private final UserRepo userRepo;
    private final MyKeywordRepo myKeywordRepo;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * 모집글 스크랩 토글
     */
    @Transactional
    public RecruitingScrapDTO.Response toggleScrap(Long recruitingId) {
        // 1. 인증된 사용자 ID 획득
        Long userId = AuthorizeUserId.getAuthorizedUserId();

        // 2. 모집글 존재 확인
        if (!recruitingRepo.existsById(recruitingId)) {
            throw new IllegalArgumentException("Recruiting not found: " + recruitingId);
        }

        // 3. 스크랩 여부 확인 후 생성/삭제
        boolean isScrap;
        String message;

        if (recruitingScrapRepository.existsByUserIdAndRecruitingId(userId, recruitingId)) {
            // 스크랩 취소
            recruitingScrapRepository.deleteByUserIdAndRecruitingId(userId, recruitingId);
            isScrap = false;
            message = "스크랩이 취소되었습니다.";
        } else {
            // 스크랩 추가
            RecruitingScrap scrap = RecruitingScrap.builder()
                    .userId(userId)
                    .recruitingId(recruitingId)
                    .build();
            recruitingScrapRepository.save(scrap);
            isScrap = true;
            message = "스크랩이 추가되었습니다.";
        }

        // 4. 응답 생성
        Long scrapCount = recruitingScrapRepository.countByRecruitingId(recruitingId);
        return RecruitingScrapDTO.Response.builder()
                .recruitingId(recruitingId)
                .isScrap(isScrap)
                .scrapCount(scrapCount)
                .message(message)
                .build();
    }

    /**
     * 스크랩한 모집글 목록 조회
     */
    @Transactional
    public List<RecruitingDTO.RecruitingRes1> findAllScrappedRecruitings() {
        // 1. 인증된 사용자 ID 획득
        Long userId = AuthorizeUserId.getAuthorizedUserId();

        // 2. 스크랩한 모집글 ID 목록 조회
        List<Long> scrapedRecruitingIds = recruitingScrapRepository.findRecruitingIdsByUserId(userId);

        if (scrapedRecruitingIds.isEmpty()) {
            return List.of();
        }

        // 3. 모집글 정보 조회
        List<Recruiting> recruitings = recruitingRepo.findAllById(scrapedRecruitingIds);

        // 4. DTO 변환
        return recruitings.stream()
                .map(r -> {
                    // 작성자 이름
                    String writerName = userRepo.findById(r.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + r.getUserId()))
                            .getName();

                    // 키워드 리스트
                    List<String> myKeywordList = myKeywordRepo
                            .findAllByRecruitingId(r.getRecruitingId()).stream()
                            .map(MyKeyword::getKeyword)
                            .toList();

                    // 날짜 포맷
                    String dateStr = formatRecruitingDate(r.getCreatedAt());

                    // 스크랩 수
                    Long scrapCount = recruitingScrapRepository.countByRecruitingId(r.getRecruitingId());

                    return RecruitingDTO.RecruitingRes1.builder()
                            .recruitingId(r.getRecruitingId())
                            .name(writerName)
                            .projectType(r.getProjectType())
                            .projectSpecific(r.getProjectSpecific())
                            .classes(r.getClasses())
                            .topic(r.getTopic())
                            .totalPeople(r.getTotalPeople())
                            .recruitPeople(r.getRecruitPeople())
                            .title(r.getTitle())
                            .myKeyword(myKeywordList)
                            .date(dateStr)
                            .scrapCount(scrapCount)
                            .build();
                })
                .toList();
    }

    /**
     * 특정 모집글의 스크랩 수 조회
     */
    public Long getScrapCount(Long recruitingId) {
        return recruitingScrapRepository.countByRecruitingId(recruitingId);
    }

    /**
     * 현재 사용자의 스크랩 여부 확인
     */
    public boolean isScrappedByCurrentUser(Long recruitingId) {
        Long userId = AuthorizeUserId.getAuthorizedUserId();
        return recruitingScrapRepository.existsByUserIdAndRecruitingId(userId, recruitingId);
    }

    /**
     * 날짜 포맷 (RecruitingService와 동일한 로직)
     */
    private String formatRecruitingDate(LocalDateTime createdAt) {
        if (createdAt == null) return null;

        LocalDate createdDateKst = createdAt.atZone(KST).toLocalDate();
        LocalDate todayKst = LocalDate.now(KST);

        if (createdDateKst.isEqual(todayKst)) {
            return createdAt.atZone(KST).format(DateTimeFormatter.ofPattern("HH:mm"));
        } else {
            return createdAt.atZone(KST).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        }
    }
}

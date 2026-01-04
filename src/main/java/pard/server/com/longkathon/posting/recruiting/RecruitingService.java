package pard.server.com.longkathon.posting.recruiting;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pard.server.com.longkathon.MyPage.user.User;
import pard.server.com.longkathon.MyPage.user.UserRepo;
import pard.server.com.longkathon.posting.myKeyword.MyKeyword;
import pard.server.com.longkathon.posting.myKeyword.MyKeywordRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RecruitingService {
    private final RecruitingRepo recruitingRepo;
    private final MyKeywordRepo myKeywordRepo;
    private final UserRepo userRepo;


    private String formatRecruitingDate(LocalDateTime createdAt) {
        if (createdAt == null) return null;

        LocalDate today = LocalDate.now();

        if (createdAt.toLocalDate().isEqual(today)) {
            // 오늘 → HH:mm
            return createdAt.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else {
            // 오늘 아님 → yyyy.MM.dd
            return createdAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        }
    }


    @Transactional
    public List<RecruitingDTO.RecruitingRes1> viewAllRecruiting() { // 모집 페이지 전체 조회
        List<Recruiting> recruitings = recruitingRepo.findAllByOrderByRecruitingIdDesc();

        return recruitings.stream()
                .map(r -> {
                    // 1) userId -> 이름
                    String writerName = userRepo.findById(r.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + r.getUserId()))
                            .getName();

                    // 2) recruitingId -> 키워드 리스트
                    List<String> myKeywordList = myKeywordRepo
                            .findAllByRecruitingId(r.getRecruitingId()).stream()
                            .map(MyKeyword::getKeyword)
                            .toList();
                    String dateStr = formatRecruitingDate(r.getDate());

                    // 3) DTO 조립
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
                            .build();
                })
                .toList();

    }



    @Transactional
    public RecruitingDTO.RecruitingRes2 viewRecruitingDetail(Long recruitingId, Long myId) { // 모집 페이지 상세 조회
        Recruiting recruiting = recruitingRepo.findById(recruitingId)
                .orElseThrow(() -> new IllegalArgumentException("Recruiting not found: " + recruitingId));

        // 작성자 이름
        String writerName = userRepo.findById(recruiting.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + recruiting.getUserId()))
                .getName();

        // date (LocalDate -> String)
        String dateStr = recruiting.getDate() == null ? null : recruiting.getDate().toString();

        // 키워드: 해당 recruitingId의 키워드 가져오기
        List<String> myKeywordList = myKeywordRepo.findAllByRecruitingId(recruitingId).stream()
                .map(MyKeyword::getKeyword)
                .toList();

        // 최근 게시글 3개(현재 글 제외) -> RecruitingRes_recentPosts DTO로 변환
        List<RecruitingDTO.RecruitingRes_recentPosts> recentPosts = recruitingRepo.findTop5ByRecruitingIdNotOrderByRecruitingIdDesc(recruitingId).stream()
                .filter(r -> !r.getRecruitingId().equals(recruitingId))  // 현재 글 제외
                .limit(5)
                .map(r -> {
                    String recentWriterName = userRepo.findById(r.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + r.getUserId()))
                            .getName();

                    String recentDateStr;
                    if (r.getDate() == null) {
                        recentDateStr = null;
                    }
                    else {
                        recentDateStr = r.getDate().toLocalDate().toString();
                    }

                    return RecruitingDTO.RecruitingRes_recentPosts.builder()
                            .recruitingId(r.getRecruitingId())
                            .name(recentWriterName)
                            .projectType(r.getProjectType())
                            .totalPeople(r.getTotalPeople())
                            .recruitPeople(r.getRecruitPeople())
                            .title(r.getTitle())
                            .date(recentDateStr)
                            .build();
                })
                .toList();

        boolean canEdit = recruiting.getUserId().equals(myId);

        return RecruitingDTO.RecruitingRes2.builder()
                .name(writerName)
                .projectType(recruiting.getProjectType())
                .projectSpecific(recruiting.getProjectSpecific())
                .classes(recruiting.getClasses())
                .topic(recruiting.getTopic())
                .totalPeople(recruiting.getTotalPeople())
                .recruitPeople(recruiting.getRecruitPeople())
                .title(recruiting.getTitle())
                .context(recruiting.getContext())
                .myKeyword(myKeywordList)
                .date(dateStr)
                .postingList(recentPosts)
                .canEdit(canEdit)
                .build();
    }


    @Transactional
    public List<RecruitingDTO.RecruitingRes3> filter( // 모집글 필터 적용
                                                      List<String> type,
                                                      List<String> departments,
                                                      String name
    ) {
        boolean hasType = type != null && !type.isEmpty();
        boolean hasDept = departments != null && !departments.isEmpty();
        boolean hasName = name != null && !name.isBlank();

        List<Recruiting> recruitings;

        // 1) 부서/이름 조건이 있으면 -> userIds 먼저 뽑는다
        if (hasDept || hasName) {
            List<Long> userIds;

            if (hasDept && hasName) {
                userIds = userRepo.findByDepartmentInAndNameContaining(departments, name)
                        .stream().map(User::getUserId).toList();
            } else if (hasDept) {
                userIds = userRepo.findByDepartmentIn(departments)
                        .stream().map(User::getUserId).toList();
            } else {
                userIds = userRepo.findByNameContaining(name)
                        .stream().map(User::getUserId).toList();
            }

            if (userIds.isEmpty()) return List.of();

            if (hasType) {
                recruitings = recruitingRepo
                        .findByUserIdInAndProjectTypeInOrderByRecruitingIdDesc(userIds, type);
            } else {
                recruitings = recruitingRepo
                        .findByUserIdInOrderByRecruitingIdDesc(userIds);
            }

        } else {
            // 2) 부서/이름 조건이 없을 때
            if (hasType) {
                recruitings = recruitingRepo
                        .findByProjectTypeInOrderByRecruitingIdDesc(type);
            } else {
                recruitings = recruitingRepo
                        .findAllByOrderByRecruitingIdDesc();
            }
        }

        // 3) 명세서(RecruitingRes3) 응답으로 변환 (date 포함!)
        return recruitings.stream()
                .map(r -> {
                    String writerName = userRepo.findById(r.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + r.getUserId()))
                            .getName();

                    List<String> myKeywordList = myKeywordRepo
                            .findAllByRecruitingId(r.getRecruitingId())
                            .stream()
                            .map(MyKeyword::getKeyword)
                            .toList();

                    String dateStr = (r.getDate() == null) ? null : r.getDate().toString();

                    return RecruitingDTO.RecruitingRes3.builder()
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
                            .build();
                })
                .toList();
    }



    @Transactional
    public List<RecruitingDTO.RecruitingRes4> viewRecruitingMine(Long myId) { // 내 모집글 조회
        List<Recruiting> recruitings = recruitingRepo.findByUserIdOrderByRecruitingIdDesc(myId);

        return recruitings.stream()
                .map(r -> {
                    String writerName = userRepo.findById(r.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + r.getUserId()))
                            .getName();

                    List<String> myKeywordList = myKeywordRepo
                            .findAllByRecruitingId(r.getRecruitingId()).stream()
                            .map(MyKeyword::getKeyword)
                            .toList();

                    // date (LocalDate -> String)
                    String dateStr = r.getDate() == null ? null : r.getDate().toString();

                    return RecruitingDTO.RecruitingRes4.builder()
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
                            .build();
                })
                .toList();
    }

    private List<String> normalizeKeywords(List<String> keywords) { // keyword 최대 10개로 제한
        if (keywords == null) return List.of();

        List<String> cleaned = keywords.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (cleaned.size() > 10) {
            throw new IllegalArgumentException("keyword는 최대 10개까지 가능합니다.");
        }
        return cleaned;
    }


    @Transactional
    public void createRecruiting(Long userId, RecruitingDTO.RecruitingReq2 req) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Recruiting savedRecruiting = recruitingRepo.save(
                Recruiting.builder()
                        .userId(user.getUserId())
                        .projectType(req.getProjectType())
                        .projectSpecific(req.getProjectSpecific())
                        .classes(req.getClasses())
                        .topic(req.getTopic())
                        .totalPeople(req.getTotalPeople())
                        .recruitPeople(req.getRecruitPeople())
                        .title(req.getTitle())
                        .context(req.getContext())
                        //.date(LocalDate.now())
                        .build()
        );

        List<String> keywords = normalizeKeywords(req.getKeyword());
        for (String kw : keywords) {
            myKeywordRepo.save(MyKeyword.builder()
                    .recruitingId(savedRecruiting.getRecruitingId())
                    .keyword(kw)
                    .build());
        }
    }


    @Transactional
    public void deleteRecruiting(Long recruitingId, Long myId) { // 모집글 삭제
        Recruiting recruiting = recruitingRepo.findById(recruitingId)
                .orElseThrow(() -> new IllegalArgumentException("Recruiting not found: " + recruitingId));

        if (!recruiting.getUserId().equals(myId)) {
            throw new IllegalArgumentException("No permission");
        }

        myKeywordRepo.deleteAllByRecruitingId(recruitingId);
        recruitingRepo.delete(recruiting);
    }

    @Transactional
    public void updateRecruiting(Long recruitingId, Long myId, RecruitingDTO.RecruitingPatchReq req) {

        Recruiting recruiting = recruitingRepo.findById(recruitingId)
                .orElseThrow(() -> new IllegalArgumentException("Recruiting not found: " + recruitingId));

        if (!recruiting.getUserId().equals(myId)) {
            throw new IllegalArgumentException("No permission");
        }

        // 1) 본문 patch
        recruiting.update(req);

        // 2) 키워드 patch 정책:
        // - req.keyword == null 이면 "키워드 변경 없음"
        // - req.keyword != null 이면 "delete 후 재저장"
        if (req.getKeyword() != null) {
            List<String> keywords = normalizeKeywords(req.getKeyword());
            myKeywordRepo.deleteAllByRecruitingId(recruitingId);

            for (String kw : keywords) {
                myKeywordRepo.save(MyKeyword.builder()
                        .recruitingId(recruitingId)
                        .keyword(kw)
                        .build());
            }
        }
    }








}

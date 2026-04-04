package pard.server.com.longkathon.likes.scrapRecruiting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RecruitingScrapRepository extends JpaRepository<RecruitingScrap, Long> {

    // 스크랩 여부 확인 (토글 판단용)
    boolean existsByUserIdAndRecruitingId(Long userId, Long recruitingId);

    // 스크랩 삭제 (토글 해제용)
    void deleteByUserIdAndRecruitingId(Long userId, Long recruitingId);

    // 특정 모집글의 총 스크랩 수
    Long countByRecruitingId(Long recruitingId);

    // N+1 문제 방지를 위한 배치 조회
    @Query("SELECT r.recruitingId as recruitingId, COUNT(r) as scrapCount " +
           "FROM RecruitingScrap r " +
           "WHERE r.recruitingId IN :recruitingIds " +
           "GROUP BY r.recruitingId")
    List<Map<String, Object>> countScrapsByRecruitingIds(List<Long> recruitingIds);

    // 사용자가 스크랩한 모집글 ID 목록
    @Query("SELECT r.recruitingId FROM RecruitingScrap r WHERE r.userId = :userId ORDER BY r.recruitingScrapId DESC")
    List<Long> findRecruitingIdsByUserId(Long userId);
}

package pard.server.com.longkathon.posting.recruiting;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecruitingRepo extends JpaRepository<Recruiting, Long> {

    // 전체 모집글 최신순
    List<Recruiting> findAllByOrderByRecruitingIdDesc();

    // 최신 3개
    List<Recruiting> findTop5ByRecruitingIdNotOrderByRecruitingIdDesc(Long recruitingId);


    // type 필터만 적용하고 싶을 때 사용
    List<Recruiting> findByProjectTypeInOrderByRecruitingIdDesc(List<String> types);

    // department/name 필터로 User를 먼저 걸러서 userIds를 만든 후, 그 userIds에 해당하는 모집글만 가져올 때 사용 (IN 조건)
    List<Recruiting> findByUserIdInOrderByRecruitingIdDesc(List<Long> userIds);

    // department/name 필터 + type 필터를 같이 적용하고 싶을 때 사용 (IN + AND 조건 결합)
    List<Recruiting> findByUserIdInAndProjectTypeInOrderByRecruitingIdDesc(List<Long> userIds, List<String> types);

    // userId로 조회 -> 내 모집글 확인
    List<Recruiting> findByUserIdOrderByRecruitingIdDesc(Long userId);


}


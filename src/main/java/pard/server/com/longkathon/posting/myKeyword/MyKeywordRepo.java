package pard.server.com.longkathon.posting.myKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MyKeywordRepo extends JpaRepository<MyKeyword, Long> {
    List<MyKeyword> findAllByRecruitingId(Long recruitingId); // 모집 페이지 전체 조회

    void deleteAllByRecruitingId(Long recruitingId); // 모집글 삭제 -> 키워드도 같이 삭제
}

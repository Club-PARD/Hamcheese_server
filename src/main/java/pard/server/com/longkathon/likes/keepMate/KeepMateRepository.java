package pard.server.com.longkathon.likes.keepMate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface KeepMateRepository extends JpaRepository<KeepMate, Long>{
    void deleteByUserIdAndKeepUserId(Long userId, Long keepMateId);

    // 찜받은 횟수가 많은 순서대로 사용자 ID 리스트 조회
    // GROUP BY keep_user_id로 그룹화 후, COUNT(*) 내림차순 정렬
    @Query(value = "SELECT keep_user_id FROM keep_mate GROUP BY keep_user_id ORDER BY COUNT(*) DESC", nativeQuery = true)
    List<Long> findMostPopularUserId();
}

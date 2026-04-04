package pard.server.com.longkathon.likes.keepMate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface KeepMateRepository extends JpaRepository<KeepMate, Long>{

    /**
     * 찜하기 존재 여부 확인 (중복 생성 방지용)
     */
    boolean existsByUserIdAndKeepUserId(Long userId, Long keepUserId);

    /**
     * 특정 사용자-찜유저 조합의 찜하기 삭제
     */
    void deleteByUserIdAndKeepUserId(Long userId, Long keepUserId);

    /**
     * 찜받은 횟수가 많은 순서대로 사용자 ID 리스트 조회
     * GROUP BY keep_user_id로 그룹화 후, COUNT(*) 내림차순 정렬
     */
    @Query(value = "SELECT keep_user_id FROM keep_mate GROUP BY keep_user_id ORDER BY COUNT(*) DESC", nativeQuery = true)
    List<Long> findMostPopularUserId();

    /**
     * 특정 유저가 찜한 유저 ID 리스트 조회 (최신순)
     * keepMateId 내림차순 = 최근 찜한 순서
     */
    @Query("SELECT km.keepUserId FROM KeepMate km " +
           "WHERE km.userId = :userId " +
           "ORDER BY km.keepMateId DESC")
    List<Long> findKeepUserIdsByUserId(@Param("userId") Long userId);
}

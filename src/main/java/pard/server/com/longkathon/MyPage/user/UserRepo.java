package pard.server.com.longkathon.MyPage.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {


    @Query(value = "SELECT * FROM user ORDER BY RAND() LIMIT 4", nativeQuery = true)
    List<User> findRandom3(); //첫 서비스 소개글 페이지에 띄울 유저 3명을 랜덤으로 가져온다.

    Optional<User> findByEmail(String email);

    //----------------------필터 기능들 -----------------------------------

    //학부, 이름 검색 필터
    List<User> findByDepartmentInAndNameContaining(List<String> departments, String name);

    List<User> findByDepartmentIn(List<String> departments);
    // name 문자열이 "포함된" 유저들을 조회한다. (부분 검색)
    List<User> findByNameContaining(String name);

    //학번 검색 필터
    List<User> findByStudentIdBetween(Long startStudentId, Long endStudentId);

    // 학부 + 학번 필터
    List<User> findByDepartmentInAndStudentIdBetween(
            List<String> departments,
            Long startStudentId,
            Long endStudentId
    );

    // 이름 + 학번 필터
    List<User> findByNameContainingAndStudentIdBetween(
            String name,
            Long startStudentId,
            Long endStudentId
    );

    // 학부 + 이름 + 학번 필터
    List<User> findByDepartmentInAndNameContainingAndStudentIdBetween(
            List<String> departments,
            String name,
            Long startStudentId,
            Long endStudentId
    );

    // 고학번 순 정렬 (studentId 오름차순: 낮은 값 = 고학번)
    List<User> findAllByOrderByStudentIdAsc();

    // 저학번 순 정렬 (studentId 내림차순: 높은 값 = 저학번)
    List<User> findAllByOrderByStudentIdDesc();
}

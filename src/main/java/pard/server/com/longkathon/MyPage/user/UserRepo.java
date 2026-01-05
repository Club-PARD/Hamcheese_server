package pard.server.com.longkathon.MyPage.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    Optional <User> findBySocialId(String socialId); //로그인에서 DB에 이미 가입된 사용자인지 확인

    //학부, 이름 검색 필터
    List<User> findByDepartmentInAndNameContaining(List<String> departments, String name);

    List<User> findByDepartmentIn(List<String> departments);
    // name 문자열이 "포함된" 유저들을 조회한다. (부분 검색)
    List<User> findByNameContaining(String name);

    @Query(value = "SELECT * FROM user ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<User> findRandom3(); //첫 서비스 소개글 페이지에 띄울 유저 3명을 랜덤으로 가져온다.
}

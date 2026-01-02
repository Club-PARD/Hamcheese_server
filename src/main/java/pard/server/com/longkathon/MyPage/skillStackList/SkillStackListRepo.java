package pard.server.com.longkathon.MyPage.skillStackList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillStackListRepo extends JpaRepository<SkillStackList, Long>{
    void deleteAllByUserId(Long userId);
    List<String> findAllByUserId(Long userId);

}

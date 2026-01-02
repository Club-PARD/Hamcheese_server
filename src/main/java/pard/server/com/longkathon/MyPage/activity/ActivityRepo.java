package pard.server.com.longkathon.MyPage.activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepo  extends JpaRepository<Activity, Long>{
    List<Activity> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);

}

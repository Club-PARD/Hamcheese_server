package pard.server.com.longkathon.poking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pard.server.com.longkathon.MyPage.introduction.Introduction;

import java.util.List;

@Repository
public interface PokingRepo extends JpaRepository<Poking, Long>{
    List<Poking> findAllByReceiveId(Long userId);

    String findBySendId(Long sendId);

}

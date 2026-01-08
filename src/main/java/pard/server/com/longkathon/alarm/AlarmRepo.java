package pard.server.com.longkathon.alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface AlarmRepo extends JpaRepository<Alarm, Long>{
    List<Alarm> findAllByReceiverId(Long userId);
}

package pard.server.com.longkathon.MyPage.userFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFileRepo extends JpaRepository<UserFile, Long>{
    UserFile findByUserId(Long userId);
}

package pard.server.com.longkathon.likes.keepMate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface KeepMateRepository extends JpaRepository<KeepMate, Long>{
}

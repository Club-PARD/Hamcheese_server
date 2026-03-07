package pard.server.com.longkathon.likes.keepMate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class KeepMateService {
    private final KeepMateRepository keepMateRepository;

    public void createKeepMate(Long userId, Long keepUserId){ //메이트 찜하기 생성
        KeepMate keepMate = KeepMate.builder()
                .userId(userId)
                .keepUserId(keepUserId)
                .build();
        keepMateRepository.save(keepMate);
    }
}

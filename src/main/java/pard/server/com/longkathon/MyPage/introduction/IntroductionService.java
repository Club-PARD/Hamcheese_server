package pard.server.com.longkathon.MyPage.introduction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;


@Service
@RequiredArgsConstructor
public class IntroductionService {
    private final IntroductionRepo introductionRepo;

    @Transactional
    public void createOrUpdate (IntroductionDTO dto) {
        Introduction intro = introductionRepo.findById(dto.getUserId()).orElse(null);
        if (intro == null) { //첫 소개글이면 생성하고
            Introduction introduction = Introduction.builder()
                    .userId(dto.getUserId())
                    .oneLine(dto.getOneLine())
                    .build();
            introductionRepo.save(introduction);
        }else{//이미 생성된게 있으면 수정하라
            intro.updateIntroduction(dto.getOneLine());
        }
    }

    public String read (Long userId) { //userDTO 생성에 사용된다.
        Introduction introduction = introductionRepo.findById(userId).orElse(null);
        if (introduction == null) {
            return null;
        }else{
            return introduction.getOneLine();
        }
    }

}

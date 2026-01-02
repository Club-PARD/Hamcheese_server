package pard.server.com.longkathon.MyPage.skillStackList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillStackListService {
    private final SkillStackListRepo skillStackListRepo;

    @Transactional
    public void deleteAndCreate (List<String> skillList, Long userId){
        skillStackListRepo.deleteAllByUserId(userId);

        // 2) DTO 리스트 -> SkillStackList 엔티티 리스트로 변환
        List<SkillStackList> skillStackList = skillList.stream()
                .map(dto -> SkillStackList.builder()
                        .userId(userId)
                        .skillName(dto)
                        .build())
                .toList();
        // 3) 한 번에 저장
        skillStackListRepo.saveAll(skillStackList);
    }

    public List<String> read (Long userId){ //해당 유저의 skillList를 모두 불러온다.
        return skillStackListRepo.findAllByUserId(userId);
    }
}

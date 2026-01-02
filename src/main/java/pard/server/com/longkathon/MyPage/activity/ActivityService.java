package pard.server.com.longkathon.MyPage.activity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepo activityRepo;
    @Transactional
    public void deleteAndCreate (List<ActivityDTO> activityList, Long userId) {
        activityRepo.deleteAllByUserId(userId);

        // 2) DTO 리스트 -> Activity 엔티티 리스트로 변환
        List<Activity> activities = activityList.stream()
                .map(dto -> Activity.builder()
                        .userId(userId)
                        .year(dto.getYear())
                        .title(dto.getTitle())
                        .link(dto.getLink())
                        .build())
                .toList();

        // 3) 한 번에 저장
        activityRepo.saveAll(activities);
    }

    public List<ActivityDTO> read(Long userId) { //해당 유저의 모든 활동내역을 불러와
        //DTO로 변환 후 리스트에 담아 리턴
        List<Activity> activity = activityRepo.findAllByUserId(userId);
        return activity.stream().map(Activity ->
                ActivityDTO.builder()
                        .year(Activity.getYear())
                        .title(Activity.getTitle())
                        .link(Activity.getLink())
                        .build())
                .toList();
    }
}

package pard.server.com.longkathon.alarm;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/alarm")
@RequiredArgsConstructor
public class AlarmController {
    private final AlarmService alarmService;

    @GetMapping("{userId}") //해당 유저에 해단 모든 거절, 수락 요청 리턴
    public List<AlarmRes> getAlarm(@PathVariable Long userId) {
        return alarmService.getAllAlarms(userId);
    }

    @DeleteMapping("{alarmId}") //알림 확인버튼 누르면 삭제
    public void delete(@PathVariable Long alarmId) {
        alarmService.deleteAlarm(alarmId);
    }
}

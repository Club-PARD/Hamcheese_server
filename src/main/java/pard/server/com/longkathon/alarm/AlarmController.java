package pard.server.com.longkathon.alarm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pard.server.com.longkathon.MyPage.user.AuthorizeUserId;

import java.util.List;


@RestController
@RequestMapping("/alarm")
@RequiredArgsConstructor
public class AlarmController {
    private final AlarmService alarmService;

    @GetMapping("") //해당 유저에 해단 모든 거절, 수락 요청 리턴
    public ResponseEntity<List<AlarmRes>> getAlarm() {
        Long myId = AuthorizeUserId.getAuthorizedUserId();
        List<AlarmRes> result = alarmService.getAllAlarms(myId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{alarmId}") //알림 확인버튼 누르면 삭제
    public ResponseEntity<Void> delete(@PathVariable Long alarmId) {
        alarmService.deleteAlarm(alarmId);
        return ResponseEntity.noContent().build();
    }
}

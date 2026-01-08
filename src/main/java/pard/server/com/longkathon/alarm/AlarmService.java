package pard.server.com.longkathon.alarm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import pard.server.com.longkathon.MyPage.user.UserRepo;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AlarmService {

    public final AlarmRepo alarmRepo;
    private final UserRepo userRepo;

    public List<AlarmRes> getAllAlarms(Long userId) { // 해당 유저의 모든 알림을 리턴
        List<Alarm> entityList = alarmRepo.findAllByReceiverId(userId);
        return entityList.stream().map(alarm ->
                AlarmRes.builder()
                        .alarmId(alarm.getAlarmId())
                        .senderName(getUserName(alarm.getSenderId()))
                        .ok(alarm.isOk())
                        .build()).toList();
    }

    public String getUserName(Long userId) {
        return userRepo.findById(userId).get().getName();
    }

    public void deleteAlarm(Long alarmId) { //알람 삭제
        alarmRepo.deleteById(alarmId);
    }
}

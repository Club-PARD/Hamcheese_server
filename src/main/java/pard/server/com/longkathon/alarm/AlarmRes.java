package pard.server.com.longkathon.alarm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
public class AlarmRes {
    private Long alarmId;

    private String senderName;

    private boolean ok;
}

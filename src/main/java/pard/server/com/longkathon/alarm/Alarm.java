package pard.server.com.longkathon.alarm;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Alarm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alarmId;

    private Long senderId; //알람을 보낸사람, 거절&수락한사람

    private Long receiverId; //알람을 받는 사람

    private boolean ok; //수락 거절여부
}

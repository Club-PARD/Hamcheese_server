package pard.server.com.longkathon.skillStackList;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class skillStackList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long skillStackListId;

    private Long userId; //누구에 대한 유저 스택인지

    private String skillName; //스택이름
}

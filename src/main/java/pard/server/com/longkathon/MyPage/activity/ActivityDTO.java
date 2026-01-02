package pard.server.com.longkathon.MyPage.activity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
public class ActivityDTO {
    private int year;
    private String title;
    private String link;
}

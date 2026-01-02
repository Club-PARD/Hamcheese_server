package pard.server.com.longkathon.MyPage.introduction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
public class IntroductionDTO {
    private Long userId; //어느 유저의 자기소개인지
    private String oneLine; //한줄자기소개
}

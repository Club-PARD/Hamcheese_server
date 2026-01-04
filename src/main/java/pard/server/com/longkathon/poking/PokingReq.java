package pard.server.com.longkathon.poking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PokingReq {
    private Long recruitingId;
    private Long myId;
}

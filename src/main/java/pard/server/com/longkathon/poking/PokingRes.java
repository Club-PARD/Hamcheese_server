package pard.server.com.longkathon.poking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PokingRes {
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class pokingRes1{
        private Long pokingId;
        private String name;
    }
}

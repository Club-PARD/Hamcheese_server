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
    public static class pokingRes1{ // 조각 건네기 생성 res
        private Long pokingId;
        private String name;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class pokingRes2{ // 내가 받은 조각 조회 res
        private Long pokingId;
        private Long recruitingId;
        private Long senderId;
        private String projectSpecific;
        private String senderName;
        private String date;
        private String imageUrl;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CanPokeRes {
        private boolean canPoke;
        private String reason; // OK / SELF / ALREADY_POKED / USER_NOT_FOUND
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PokingResponseResult {
        private boolean accepted;       // 수락 여부
        private Long chatRoomId;       // 수락 시 생성된 채팅방 ID (거절 시 null)
        private String message;        // 응답 메시지
    }

}

package pard.server.com.longkathon.posting.myKeyword;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


public class MyKeywordDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyKeywordUpdateReq {
        private String keyword; // 새 키워드 리스트
        private Long myId;              // 로그인 유저 (권한 체크용)
    }
}

package pard.server.com.longkathon.MyPage.peerReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class PeerReviewDTO {
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PeerReviewReq1 {
        private String startDate;
        private String meetSpecific;

        @Builder.Default
        private List<String> goodKeywordList = new ArrayList<>();

        @Builder.Default
        private List<String> badKeywordList = new ArrayList<>();
    }
    }

package pard.server.com.longkathon.MyPage.user;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pard.server.com.longkathon.MyPage.activity.ActivityDTO;
import pard.server.com.longkathon.MyPage.peerReview.PeerReviewDTO;
import pard.server.com.longkathon.poking.PokingRes;
import pard.server.com.longkathon.posting.recruiting.RecruitingDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDTO {
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRes1 { // 메이트 프로필 페이지
        //자기소개 게시물 눌렀을때 해당 계정의 프로필로 이동)
        //1. 프로필관리 섹션
        private String name;
        private String email;
        private String department;
        private String firstMajor;
        private String secondMajor;
        private String gpa;
        private String studentId;
        private String semester;
        private String imageUrl; //****
        private String grade;

        //2. 자기소개 섹션
        private String introduction;
        @Builder.Default
        private List<String> skillList = new ArrayList<>();

        //3. 활동내역 섹션
        @Builder.Default
        private List<ActivityDTO> activity = new ArrayList<>();

        //4. 동료평가 섹션
        @Builder.Default
        private Map<String, Integer> peerGoodKeyword = new HashMap<>();
        private int goodKeywordCount; //유저가 받은 긍정 키워드 총 개수

        @Builder.Default
        private Map<String, Integer> peerBadKeyword = new HashMap<>();
        private int badKeywordCount; //유저가 받은 부정 키워드 총 개수

        @Builder.Default //동료평가 최신순으로 담은 리스트
        private List<PeerReviewDTO.PeerReviewReq1> peerReviewRecent = new ArrayList<>();
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRes2{ //회원가입 성공 후 리턴되는 로그인된 계정 id, 사용자 이름, 유저 프로필 URL
        private Long myId;
        private String name;
        private String imageUrl;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRes3 { // 마이 프로필 페이지
        //자기소개 게시물 눌렀을때 해당 계정의 프로필로 이동)
        //1. 프로필관리 섹션
        private String name;
        private String email;
        private String department;
        private String firstMajor;
        private String secondMajor;
        private String gpa;
        private String studentId;
        private String grade;
        private String semester;
        private String imageUrl;

        //2. 자기소개 섹션
        private String introduction;
        @Builder.Default
        private List<String> skillList = new ArrayList<>();

        //3. 활동내역 섹션
        @Builder.Default
        private List<ActivityDTO> activity = new ArrayList<>();
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRes4{ //마이 페이지의 나의 동료평가 탭
        //동료평가 탭
        @Builder.Default
        private Map<String, Integer> peerGoodKeyword = new HashMap<>();
        private int goodKeywordCount; //유저가 받은 긍정 키워드 총 개수

        @Builder.Default
        private Map<String, Integer> peerBadKeyword = new HashMap<>();
        private int badKeywordCount; //유저가 받은 부정 키워드 총 개수
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRes5{ //전체 메이트 조회 (메이트 둘러보기 페이지)
        private Long userId;
        private String name;
        private String firstMajor;
        private String secondMajor;
        private String studentId;
        private String introduction;

        @Builder.Default
        private List<String> skillList = new ArrayList<>();
        @Builder.Default
        private Map<String, Integer> peerGoodKeywords = new HashMap<>();

        private String imageUrl;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRes6{ //첫 서비스 소개글 페이지에 띄울 profileFeedList,recruitingFeedList
        @Builder.Default
        private List<UserRes5> profileFeedList = new ArrayList<>();
        @Builder.Default
        private List<RecruitingDTO.RecruitingRes5> recruitingFeedList = new ArrayList<>();
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserReq1{ //회원가입할때 받는 유저 정보
        private String name;
        private String studentId;
        private String grade;
        private String semester;
        private String department;
        private String firstMajor;
        private String secondMajor;
        private String phoneNumber;
        private String gpa;

        private String email;
        private String socialId;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserReq2{ //메이트 둘러보기 화면의 필터링 2개
        @Builder.Default
        private List<String> department = new ArrayList<>(); //학부 필터

        private String name; //이름 검색
    }


}

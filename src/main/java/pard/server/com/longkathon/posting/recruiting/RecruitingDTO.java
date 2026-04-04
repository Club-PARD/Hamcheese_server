package pard.server.com.longkathon.posting.recruiting;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class RecruitingDTO {
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecruitingReq1 { // 모집하기 페이지 필터 req
        private List<String> type;
        private String department;
        private String name;

    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecruitingReq2 { // 모집하기 페이지 생성 req
        private String projectType;
        private String projectSpecific;
        private String classes;
        private String topic;
        private int totalPeople;
        private int recruitPeople;
        private String title;
        private String context;
        @Builder.Default
        private List<String> myKeyword = new ArrayList<>();
    }

    @Builder @Getter @AllArgsConstructor @NoArgsConstructor
    public static class RecruitingPatchReq {
        private String projectType;
        private String projectSpecific;
        private String classes;
        private String topic;
        private Integer totalPeople;
        private Integer recruitPeople;
        private String title;
        private String context;
        private List<String> keyword;
    }


    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecruitingRes_recentPosts { // 최근 게시글에 대한 res
        private Long recruitingId;
        private String name;
        private String projectType;
        private int totalPeople;//전체인원
        private int recruitPeople;//모집인원
        private String title;//제목
        private String date;
    }
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecruitingRes1 { // 모집하기 페이지 전체조회 res
        private Long recruitingId;
        private String name;
        private String projectType;
        private String projectSpecific;// 구체적인 이름 (수업 이름)
        private String classes;//분반
        private String topic;//주제
        private int totalPeople;//전체인원
        private int recruitPeople;//모집인원
        private String title;//제목
        @Builder.Default
        private List<String> myKeyword = new ArrayList<>();
        private String date;
        private Long scrapCount; // 스크랩 수

    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecruitingRes2 { // 모집글 상세페이지 res
        private String name;
        private String projectType;
        private String projectSpecific;
        private String classes;
        private String topic;
        private int totalPeople;
        private int recruitPeople;
        private String title;
        private String context;
        private Long studentId;
        private String firstMajor;
        private String secondMajor;
        private String imageUrl;
        @Builder.Default
        private List<String> myKeyword = new ArrayList<>();
        private String date;
        @Builder.Default
        private List<RecruitingDTO.RecruitingRes_recentPosts> postingList = new ArrayList<>();
        private Boolean canEdit;
        private Boolean isScrap; // 현재 사용자의 스크랩 여부
    }


    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecruitingRes5 { //맨처음 서비스 소개 글에 띄울 모집글dto
        private Long recruitingId;
        private String name;
        private String projectType;
        private String projectSpecific;
        private String classes;
        private String topic;
        private int totalPeople;
        private int recruitPeople;
        private String title;
    }







}

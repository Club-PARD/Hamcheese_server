package pard.server.com.longkathon.MyPage.peerReview;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import pard.server.com.longkathon.MyPage.peerBadKeyword.PeerBadKeyword;
import pard.server.com.longkathon.MyPage.peerBadKeyword.PeerBadKeywordRepo;
import pard.server.com.longkathon.MyPage.peerGoodKeyword.PeerGoodKeyword;
import pard.server.com.longkathon.MyPage.peerGoodKeyword.PeerGoodKeywordRepo;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PeerReviewService {
    private final PeerReviewRepo peerReviewRepo;
    private final PeerGoodKeywordRepo peerGoodKeywordRepo;
    private final PeerBadKeywordRepo peerBadKeywordRepo;

    public Map<String, Integer> goodKeyword (Long userId){
        List<PeerGoodKeyword> allList = peerGoodKeywordRepo.findAllByUserIdOrderByCountDesc(userId);
        //모든 긍정 키워드 PeerGoodKeyword를 가져온다.

        Map<String, Integer> result = new LinkedHashMap<>();
        //가져온 엔티티들의 key-value페어를 keyword - count로 맞춘 맵을 형성

        for (PeerGoodKeyword e : allList) {
            result.put(e.getKeyword(), e.getCount()); // keyword(String) -> count(int)
        }
        return result;
    }

    //메이트 둘러보기 페이지에 3개만 보일 긍정 키워드
    public Map<String, Integer>  goodKeywordTop3 (Long userId){
        List<PeerGoodKeyword> allList = peerGoodKeywordRepo.findTop3ByUserIdOrderByCountDesc(userId);
        //count의 개수가 가장많은 상위 3개의 PeerGoodKeyword를 가져온다.

        Map<String, Integer> result = new LinkedHashMap<>();
        //가져온 엔티티들의 key-value페어를 keyword - count로 맞춘 맵을 형성

        for (PeerGoodKeyword e : allList) {
            result.put(e.getKeyword(), e.getCount()); // keyword(String) -> count(int)
        }
        return result;

    }

    public int goodKeywordCount (Long userId){
        int total = 0;
        //해당 유저에 대한 모든 긍정키워드 엔티티들를 찾는다.
        List<PeerGoodKeyword> goodKeywordList = peerGoodKeywordRepo.findByUserId(userId);

        //그렇게 찾은 엔티티들의 count필드를 더한다.
        for (PeerGoodKeyword e : goodKeywordList) {
            total += e.getCount();
        }

        return total;
    }

    public Map<String, Integer> BadKeyword (Long userId){ //모든 부정키워드
        List<PeerBadKeyword> allList = peerBadKeywordRepo.findAllByUserIdOrderByCountDesc(userId);
        //모든 부정키워드 PeerBadKeyword 가져온다.

        Map<String, Integer> result = new LinkedHashMap<>();
        //가져온 엔티티들의 key-value페어를 keyword - count로 맞춘 맵을 형성

        for (PeerBadKeyword e : allList) {
            result.put(e.getKeyword(), e.getCount()); // keyword(String) -> count(int)
        }
        return result;
    }

    public int badKeywordCount (Long userId){
        int total = 0;
        //해당 유저에 대한 모든 부정 키워드 엔티티들를 찾는다.
        List<PeerBadKeyword> badKeywordList = peerBadKeywordRepo.findByUserId(userId);

        //그렇게 찾은 엔티티들의 count필드를 더한다.
        for (PeerBadKeyword e : badKeywordList) {
            total += e.getCount();
        }

        return total;
    }

    @Transactional
    public void createPeerReview(Long myId, Long userId, PeerReviewDTO.PeerReviewReq1 req) {

        PeerReview peerReview = PeerReview.builder()
                .startDate(req.getStartDate())
                .meetSpecific(req.getMeetSpecific())
                .writerId(myId)
                .userId(userId)
                .build();

        peerReviewRepo.save(peerReview);

        upsertGoodKeywords(userId, req.getGoodKeywordList());
        upsertBadKeywords(userId, req.getBadKeywordList());
    }

    private void upsertGoodKeywords(Long userId, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return;

        // 1) 입력 정리 + 빈값 제거 + 같은 키워드 중복 횟수 집계
        Map<String, Long> freq = keywords.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        if (freq.isEmpty()) return;

        // 2) 기존 row들을 한 번에 조회
        List<PeerGoodKeyword> existing = peerGoodKeywordRepo
                .findAllByUserIdAndKeywordIn(userId, freq.keySet());

        Map<String, PeerGoodKeyword> existingMap = existing.stream()
                .collect(Collectors.toMap(PeerGoodKeyword::getKeyword, e -> e));

        // 3) 있으면 count 증가, 없으면 새로 생성
        List<PeerGoodKeyword> toSave = new ArrayList<>();

        for (Map.Entry<String, Long> entry : freq.entrySet()) {
            String keyword = entry.getKey();
            int delta = entry.getValue().intValue();

            PeerGoodKeyword e = existingMap.get(keyword);
            if (e != null) {
                e.increaseCount(delta);   // ✅ count + delta
                toSave.add(e);
            } else {
                toSave.add(PeerGoodKeyword.builder()
                        .userId(userId)
                        .keyword(keyword)
                        .count(delta)     // ✅ 처음엔 delta로 생성
                        .build());
            }
        }

        peerGoodKeywordRepo.saveAll(toSave);
    }

    private void upsertBadKeywords(Long userId, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return;

        Map<String, Long> freq = keywords.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        if (freq.isEmpty()) return;

        List<PeerBadKeyword> existing = peerBadKeywordRepo
                .findAllByUserIdAndKeywordIn(userId, freq.keySet());

        Map<String, PeerBadKeyword> existingMap = existing.stream()
                .collect(Collectors.toMap(PeerBadKeyword::getKeyword, e -> e));

        List<PeerBadKeyword> toSave = new ArrayList<>();

        for (Map.Entry<String, Long> entry : freq.entrySet()) {
            String keyword = entry.getKey();
            int delta = entry.getValue().intValue();

            PeerBadKeyword e = existingMap.get(keyword);
            if (e != null) {
                e.increaseCount(delta);
                toSave.add(e);
            } else {
                toSave.add(PeerBadKeyword.builder()
                        .userId(userId)
                        .keyword(keyword)
                        .count(delta)
                        .build());
            }
        }

        peerBadKeywordRepo.saveAll(toSave);
    }

}

package pard.server.com.longkathon.posting.myKeyword;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pard.server.com.longkathon.posting.recruiting.Recruiting;
import pard.server.com.longkathon.posting.recruiting.RecruitingRepo;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyKeywordService {

    private final RecruitingRepo recruitingRepo;
    private final MyKeywordRepo myKeywordRepo;

    private void validateKeywords(List<String> keywords) { // keyword 개수 10개로 제한
        if (keywords == null) return;
        if (keywords.size() > 10) {
            throw new IllegalArgumentException("Keyword can be up to 10");
        }
    }

    private void saveKeywords(Long recruitingId, List<String> keywords) { // keyword 저장하기
        if (keywords == null) return;

        validateKeywords(keywords);

        for (String kw : keywords) {
            if (kw == null || kw.isBlank()) continue;

            myKeywordRepo.save(
                    MyKeyword.builder()
                            .recruitingId(recruitingId)
                            .keyword(kw.trim())
                            .build()
            );
        }
    }

    // 모집글에서 keyword만 수정, 이 기능 추가하려면 controller도 필요하지만 현재는 이 기능 보류
    @Transactional
    public void updateKeywordsOnly(Long recruitingId, Long myId, List<String> newKeywords) {

        Recruiting recruiting = recruitingRepo.findById(recruitingId)
                .orElseThrow(() -> new IllegalArgumentException("Recruiting not found: " + recruitingId));

        // 권한 체크
        if (!recruiting.getUserId().equals(myId)) {
            throw new IllegalArgumentException("No permission");
        }

        validateKeywords(newKeywords);

        // 기존 키워드 삭제 후 재저장
        myKeywordRepo.deleteAllByRecruitingId(recruitingId);
        saveKeywords(recruitingId, newKeywords);
    }

    public List<String> readKeywords(Long recruitingId) { //해당 구인글에 속한 키워드들의 리스트를 리턴
        List<MyKeyword> keywordList = myKeywordRepo.findAllByRecruitingId(recruitingId);
        List<String> keywords = keywordList.stream()
                .map(MyKeyword::getKeyword)   // String keyword 필드만 추출
                .toList();

        return keywords;
    }
}

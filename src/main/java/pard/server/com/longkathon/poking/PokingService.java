package pard.server.com.longkathon.poking;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pard.server.com.longkathon.MyPage.user.User;
import pard.server.com.longkathon.MyPage.user.UserRepo;
import pard.server.com.longkathon.posting.recruiting.Recruiting;
import pard.server.com.longkathon.posting.recruiting.RecruitingRepo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PokingService {

    private final PokingRepo pokingRepo;
    private final UserRepo userRepo;
    private final RecruitingRepo recruitingRepo;

    /**
     * UserDTO에서 사용: 특정 유저(받는 사람)가 받은 모든 찌르기를 DTO 리스트로 반환
     * - sender 이름은 User 테이블에서 조회
     */
    @Transactional
    public List<PokingRes.pokingRes1> getPoking(Long userId) {
        List<Poking> pokingList = pokingRepo.findAllByReceiveIdOrderByPokingIdDesc(userId);
        if (pokingList.isEmpty()) return List.of();

        // senderId -> name 한번에 조회 (N+1 방지)
        List<Long> senderIds = pokingList.stream()
                .map(Poking::getSendId)
                .distinct()
                .toList();

        Map<Long, String> senderNameById = userRepo.findAllById(senderIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getName));

        return pokingList.stream()
                .map(p -> PokingRes.pokingRes1.builder()
                        .pokingId(p.getPokingId())
                        .name(senderNameById.getOrDefault(p.getSendId(), "Unknown"))
                        .build())
                .toList();
    }


    /**
     * (내부 공용) sender/receiver로 찌르기 생성 + sender 이름 반환
     */
    @Transactional
    public PokingRes.pokingRes1 createPoking(Long recruitingId, Long myId) {
        if (recruitingId.equals(myId)) {
            throw new IllegalArgumentException("자기 자신을 찌를 수 없습니다.");
        }

        // sender/receiver 존재 검증
        User sender = userRepo.findById(myId)
                .orElseThrow(() -> new IllegalArgumentException("sender user not found: " + myId));

        userRepo.findById(recruitingId)
                .orElseThrow(() -> new IllegalArgumentException("receiver user not found: " + recruitingId));

        // 중복 찌르기 방지
        if (pokingRepo.existsBySendIdAndReceiveId(myId, recruitingId)) {
            throw new IllegalStateException("이미 찌르기를 보냈습니다.");
        }

        Poking saved = pokingRepo.save(
                Poking.builder()
                        .sendId(myId)
                        .receiveId(recruitingId)
                        .build()
        );

        return PokingRes.pokingRes1.builder()
                .pokingId(saved.getPokingId())
                .name(sender.getName())
                .build();
    }
}

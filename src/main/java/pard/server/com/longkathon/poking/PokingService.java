package pard.server.com.longkathon.poking;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pard.server.com.longkathon.MyPage.user.User;
import pard.server.com.longkathon.MyPage.user.UserRepo;
import pard.server.com.longkathon.posting.recruiting.Recruiting;
import pard.server.com.longkathon.posting.recruiting.RecruitingRepo;

import java.time.Duration;
import java.time.LocalDateTime;
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
        if (pokingRepo.existsBySendIdAndRecruitingId(myId, recruitingId)) {
            throw new IllegalStateException("이미 찌르기를 보냈습니다.");
        }

        Poking saved = pokingRepo.save(
                Poking.builder()
                        .sendId(myId)
                        .receiveId(recruitingId)
                        .recruitingId(recruitingId)
                        .date(LocalDateTime.now())
                        .build()
        );

        return PokingRes.pokingRes1.builder()
                .pokingId(saved.getPokingId())
                .name(sender.getName())
                .build();
    }

    @Transactional
    public PokingRes.pokingRes1 createPokingToUser(Long userId, Long myId) {
        if (myId.equals(userId)) throw new IllegalArgumentException("자기 자신은 찌를 수 없습니다.");

        User receiver = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("receiver user not found: " + userId));
        User sender = userRepo.findById(myId)
                .orElseThrow(() -> new IllegalArgumentException("sender user not found: " + myId));

        if (pokingRepo.existsBySendIdAndReceiveIdAndRecruitingIdIsNull(myId, userId)) {
            throw new IllegalStateException("이미 찌르기를 보냈습니다.");
        }

        Poking saved = pokingRepo.save(Poking.builder()
                .sendId(myId)
                .receiveId(userId)
                .recruitingId(null)
                .date(LocalDateTime.now())
                .build());

        return PokingRes.pokingRes1.builder()
                .pokingId(saved.getPokingId())
                .name(sender.getName())
                .build();
    }

    private String toRelativeTime(LocalDateTime date) { // 시간 ~전 으로 표시
        if (date == null) return null;

        LocalDateTime now = LocalDateTime.now(); // 서버 기준 시간
        Duration d = Duration.between(date, now);

        long seconds = d.getSeconds();
        if (seconds < 0) seconds = 0;

        if (seconds < 60) return "방금 전";

        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "분 전";

        long hours = minutes / 60;
        if (hours < 24) return hours + "시간 전";

        long days = hours / 24;
        if (days < 7) return days + "일 전";

        long weeks = days / 7;
        if (weeks < 5) return weeks + "주 전";

        long months = days / 30;
        if (months < 12) return months + "개월 전";

        long years = days / 365;
        return years + "년 전";
    }

    @Transactional
    public java.util.List<PokingRes.pokingRes2> received(Long myId) {
        List<Poking> list = pokingRepo.findAllByReceiveIdOrderByPokingIdDesc(myId);
        if (list.isEmpty()) return List.of();

        List<Long> senderIds = list.stream().map(Poking::getSendId).distinct().toList();
        Map<Long, String> senderNameById = userRepo.findAllById(senderIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getName));

        return list.stream()
                .map(p -> PokingRes.pokingRes2.builder()
                        .pokingId(p.getPokingId())
                        .recruitingId(p.getRecruitingId())
                        .senderId(p.getSendId())
                        .senderName(senderNameById.getOrDefault(p.getSendId(), "Unknown"))
                        .date(toRelativeTime(p.getDate()))
                        .build())
                .toList();
    }

    @Transactional
    public void delete(Long pokingId) {
        pokingRepo.deleteById(pokingId);
    }
}

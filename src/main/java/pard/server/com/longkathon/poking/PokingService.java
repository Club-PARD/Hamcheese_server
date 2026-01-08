package pard.server.com.longkathon.poking;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pard.server.com.longkathon.MyPage.user.User;
import pard.server.com.longkathon.MyPage.user.UserRepo;
import pard.server.com.longkathon.MyPage.userFile.UserFileService;
import pard.server.com.longkathon.alarm.Alarm;
import pard.server.com.longkathon.alarm.AlarmRepo;
import pard.server.com.longkathon.posting.recruiting.Recruiting;
import pard.server.com.longkathon.posting.recruiting.RecruitingRepo;
import pard.server.com.longkathon.posting.recruiting.RecruitingService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PokingService {

    private final PokingRepo pokingRepo;
    private final UserRepo userRepo;
    private final RecruitingRepo recruitingRepo;
    private final RecruitingService recruitingService;
    private final UserFileService userFileService;
    private final AlarmRepo alarmRepo;

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

        // sender/receiver 존재 검증
        User sender = userRepo.findById(myId)
                .orElseThrow(() -> new IllegalArgumentException("sender user not found: " + myId));

        recruitingRepo.findById(recruitingId)
                .orElseThrow(() -> new IllegalArgumentException("receiver user not found: " + recruitingId));

        Optional<Recruiting> recruiting = recruitingRepo.findById(recruitingId);

        Poking saved = pokingRepo.save(
                Poking.builder()
                        .sendId(myId)
                        .receiveId(recruiting.get().getUserId())
                        .recruitingId(recruitingId)
                        .date(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                        .build()
        );

        return PokingRes.pokingRes1.builder()
                .pokingId(saved.getPokingId())
                .name(sender.getName())
                .build();
    }

    @Transactional //유저 프로필에서 찌르기
    public PokingRes.pokingRes1 createPokingToUser(Long userId, Long myId) {
        User receiver = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("receiver user not found: " + userId));
        User sender = userRepo.findById(myId)
                .orElseThrow(() -> new IllegalArgumentException("sender user not found: " + myId));

        Poking saved = pokingRepo.save(Poking.builder()
                .sendId(myId)
                .receiveId(userId)
                .recruitingId(null)
                .date(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build());

        return PokingRes.pokingRes1.builder()
                .pokingId(saved.getPokingId())
                .name(sender.getName())
                .build();
    }

    @Transactional
    public PokingRes.CanPokeRes canPokeProfile(Long userId, Long myId) {
        // 1) 자기 자신
        if (myId.equals(userId)) {
            return PokingRes.CanPokeRes.builder()
                    .canPoke(false)
                    .reason("SELF")
                    .build();
        }

        // 2) 유저 존재 확인
        if (!userRepo.existsById(userId)) {
            return PokingRes.CanPokeRes.builder()
                    .canPoke(false)
                    .reason("USER_NOT_FOUND")
                    .build();
        }
        if (!userRepo.existsById(myId)) {
            return PokingRes.CanPokeRes.builder()
                    .canPoke(false)
                    .reason("USER_NOT_FOUND")
                    .build();
        }

        // 3) 이미 찌른 상태인지
        boolean already = pokingRepo.existsBySendIdAndReceiveId(myId, userId);
        if (already) {
            return PokingRes.CanPokeRes.builder()
                    .canPoke(false)
                    .reason("ALREADY_POKED")
                    .build();
        }
        // 4) 가능
        return PokingRes.CanPokeRes.builder()
                .canPoke(true)
                .reason("OK")
                .build();
    }

    @Transactional
    public PokingRes.CanPokeRes canPokeRecruiting(Long recruitingId, Long myId) {
        //게시글 id로 게시글 주인을 찾아서 그 사람과 내 id로 생성된 찌르기가 있는지 확인
        Optional<Recruiting> recruiting = recruitingRepo.findById(recruitingId);
        Long recId = recruiting.get().getUserId();
        // 1) 자기 자신
        if (myId.equals(recId)) {
            return PokingRes.CanPokeRes.builder()
                    .canPoke(false)
                    .reason("SELF")
                    .build();
        }

        // 2) 유저 존재 확인
        if (!userRepo.existsById(recId)) {
            return PokingRes.CanPokeRes.builder()
                    .canPoke(false)
                    .reason("USER_NOT_FOUND")
                    .build();
        }
        if (!userRepo.existsById(myId)) {
            return PokingRes.CanPokeRes.builder()
                    .canPoke(false)
                    .reason("USER_NOT_FOUND")
                    .build();
        }

        // 3) 이미 찌른 상태인지
        boolean already = pokingRepo.existsBySendIdAndReceiveId(myId, recId);
        if (already) {
            return PokingRes.CanPokeRes.builder()
                    .canPoke(false)
                    .reason("ALREADY_POKED")
                    .build();
        }
        // 4) 가능
        return PokingRes.CanPokeRes.builder()
                .canPoke(true)
                .reason("OK")
                .build();
    }


    private String toRelativeTime(LocalDateTime date) { // 시간 ~전 으로 표시
        if (date == null) return null;

        LocalDateTime now = LocalDateTime.now(); // 서버 기준 시간
        Duration d = Duration.between(date, now);

        long seconds = d.getSeconds();
        if (seconds < 0) seconds = 0;

        if (seconds < 60) return "방금전";

        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "분전";

        long hours = minutes / 60;
        if (hours < 24) return hours + "시간전";

        long days = hours / 24;
        if (days < 7) return days + "일전";

        long weeks = days / 7;
        if (weeks < 5) return weeks + "주전";

        long months = days / 30;
        if (months < 12) return months + "개월전";

        long years = days / 365;
        return years + "년전";
    }

    @Transactional
    public java.util.List<PokingRes.pokingRes2> received(Long myId) {
        List<Poking> list = pokingRepo.findAllByReceiveIdOrderByPokingIdDesc(myId);
        if (list.isEmpty()) return List.of();

        List<Long> senderIds = list.stream().map(Poking::getSendId).distinct().toList();
        Map<Long, String> senderNameById = userRepo.findAllById(senderIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getName));

        return list.stream()
                .map(p -> {
                    String projectSpecific = null;
                    if (p.getRecruitingId() != null) {
                        projectSpecific = recruitingService.readTitle(p.getRecruitingId());
                    }

                    return PokingRes.pokingRes2.builder()
                            .pokingId(p.getPokingId())
                            .recruitingId(p.getRecruitingId())
                            .senderId(p.getSendId())
                            .senderName(senderNameById.getOrDefault(p.getSendId(), "Unknown"))
                            .date(recruitingService.koreaTime(p.getDate()))
                            .projectSpecific(projectSpecific)
                            .imageUrl(userFileService.getURL(p.getSendId()))
                            .build();
                })
                .toList();
    }

    @Transactional //삭제 할때 수락, 거절 여부에 따라 알림을 생성한다.
    public void delete(Long pokingId, PokingReq pokingReq) {
        Poking poking = pokingRepo.findById(pokingId).get(); //헤당 찌르기를 찾아서
        User sender = userRepo.findById(poking.getReceiveId()).get(); // 찌르기를 받는 사람이 알림을 보내는사람이 된다.
        User receiver = userRepo.findById(poking.getSendId()).get(); //찌르기를 보내는 사람이 알림을 받는 사람이된다.

        if (pokingReq.isOk()){ // 수락이면
            Alarm alarm = Alarm.builder()
                    .senderId(sender.getUserId())
                    .receiverId(receiver.getUserId())
                    .ok(pokingReq.isOk())
                    .build();
            alarmRepo.save(alarm);
        }else{
            Alarm alarm = Alarm.builder()
                    .senderId(sender.getUserId())
                    .receiverId(receiver.getUserId())
                    .ok(pokingReq.isOk())
                    .build();
            alarmRepo.save(alarm);
        }
        pokingRepo.deleteById(pokingId);
    }
}

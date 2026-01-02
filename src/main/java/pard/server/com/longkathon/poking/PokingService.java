package pard.server.com.longkathon.poking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PokingService {
    private final PokingRepo pokingRepo;

    //userDTO에서 사용
    public List<PokingRes.pokingRes1> getPoking(Long userId) { //특정 유저가 받은 모든 찌르기를 리스트로 리턴
        List<Poking> pokingList = pokingRepo.findAllByReceiveId(userId); //특정 유저가 받은 모든찌르기 엔티티를 찾는다.

        return pokingList.stream().map(Poking -> //찾은 엔티티들을 DTO로 변환 후 리스트로 리턴
                PokingRes.pokingRes1.builder()
                        .pokingId(Poking.getPokingId())
                        .name(pokingRepo.findBySendId(Poking.getSendId()))
                        .build())
                .toList();
    }
}

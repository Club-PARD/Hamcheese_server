package pard.server.com.longkathon.poking;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PokingRepo extends JpaRepository<Poking, Long> {

    // 특정 유저(받는 사람)가 받은 모든 찌르기 최신순
    List<Poking> findAllByReceiveIdOrderByPokingIdDesc(Long receiveId);

    // 프로필 중복 찌르기 방지
    boolean existsBySendIdAndReceiveIdAndRecruitingIdIsNull(Long sendId, Long receiveId);

    // 중복 찌르기 방지
    boolean existsBySendIdAndRecruitingId(Long sendId, Long recruitingId);
}

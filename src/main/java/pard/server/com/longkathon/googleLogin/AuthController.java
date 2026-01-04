package pard.server.com.longkathon.googleLogin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pard.server.com.longkathon.MyPage.user.UserRepo;
import pard.server.com.longkathon.MyPage.user.User;
import pard.server.com.longkathon.MyPage.userFile.UserFile;
import pard.server.com.longkathon.MyPage.userFile.UserFileRepo;
import pard.server.com.longkathon.MyPage.userFile.UserFileService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/auth")
public class AuthController {

    private final UserRepo userRepo;
    private final UserFileRepo userFileRepo;
    private final UserFileService userFileService;

    @PostMapping("/google/exists")
    public GoogleLoginExistsRes googleExists(@RequestBody GoogleIdTokenReq req) throws Exception {

        // 요청 1건 추적용 ID
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        log.info("[{}][01] /auth/google/exists 요청 도착", traceId);

        // 요청 바디 체크
        String idToken = req.getIdToken();
        if (idToken == null) {
            log.warn("[{}][02] idToken=null (요청 바디 확인 필요)", traceId);
            throw new IllegalArgumentException("idToken is null");
        }
        log.info("[{}][02] idToken 수신 완료 (length={})", traceId, idToken.length());

        try {
            log.info("[{}][03] GoogleTokenParser.parse() 시작", traceId);
            GoogleUserInfo info = GoogleTokenParser.parse(idToken, traceId);
            log.info("[{}][06] 파싱 완료: email={}, socialId={}", traceId, info.getEmail(), info.getSocialId());

            log.info("[{}][07] DB 조회 시작: findBySocialId({})", traceId, info.getSocialId());
            Optional<User> optUser = userRepo.findBySocialId(info.getSocialId());

            boolean exists = optUser.isPresent();
            Long userId = optUser.map(User::getUserId).orElse(null);
            log.info("[{}][08] DB 조회 완료: exists={}, userId={}", traceId, exists, userId);

            // ✅ user가 없으면 null로 내려주기
            String name = optUser.map(User::getName).orElse(null);

            // ✅ userId가 null이면 URL 조회 자체를 안 함
            String url = null;
            if (userId != null) {
                log.info("[{}][08-1] 프로필 URL 조회 시작: userFileService.getURL({})", traceId, userId);
                url = userFileService.getURL(userId);
                log.info("[{}][08-2] 프로필 URL 조회 완료: url={}", traceId, url);
            } else {
                log.info("[{}][08-1] userId=null 이므로 프로필 URL 조회 스킵", traceId);
            }

            GoogleLoginExistsRes res =
                    new GoogleLoginExistsRes(exists, info.getEmail(), info.getSocialId(), userId, name, url);

            log.info("[{}][09] 응답 DTO 생성 완료 -> exists={}, userId={}, name={}, url={}, email={}, socialId={}",
                    traceId, exists, userId, name, url, info.getEmail(), info.getSocialId());

            return res;

        } catch (Exception e) {
            log.error("[{}][ERR] 처리 중 예외 발생: {}", traceId, e.getMessage(), e);
            throw e;
        }
    }
}

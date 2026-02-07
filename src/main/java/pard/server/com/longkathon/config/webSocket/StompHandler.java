package pard.server.com.longkathon.config.webSocket;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import pard.server.com.longkathon.MyPage.user.UserRepo;
import pard.server.com.longkathon.config.jwt.TokenProvider;

@Slf4j
@Component
@RequiredArgsConstructor
//WebSocket 연결 후 STOMP 프로토콜을 통신시 JWT 인증/인가 처리를 수행하기 위해 Spring의 ChannelInterceptor를 구현한 StompHandler를 사용했습니다.
//이 핸들러는 클라이언트로부터 서버에 들어오는 메시지를 가로채 필요한 검증 로직을 수행합니다.
public class StompHandler implements ChannelInterceptor {
    private final UserRepo userRepository;
    private final TokenProvider jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String auth = accessor.getFirstNativeHeader("Authorization");
            if (auth == null) auth = accessor.getFirstNativeHeader("authorization"); // 혹시 모를 케이스

            if (auth == null || !auth.startsWith("Bearer ")) {
                throw new MessagingException("Authorization 헤더가 없거나 Bearer 형식이 아닙니다.");
            }

            try {
                String jwt = jwtUtil.substringToken(auth);

                if (!jwtUtil.validToken(jwt)) {
                    throw new MessagingException("JWT 인증 실패(유효하지 않은 토큰)");
                }

                Claims claims = jwtUtil.getClaims(jwt);
                if (claims == null) throw new MessagingException("claims is null");

                String sub = claims.getSubject();

                Long userId = null;

                // 1) sub가 숫자면 userId
                if (sub != null && sub.matches("\\d+")) {
                    userId = Long.valueOf(sub);
                } else {
                    // 2) userId 클레임이 있으면 우선 사용 (Long/Integer 모두 커버)
                    Number n = claims.get("userId", Number.class);
                    if (n != null) userId = n.longValue();

                    // 3) email 클레임이 없으면 sub를 email로 사용
                    String email = claims.get("email", String.class);
                    if (email == null) email = sub;

                    if (userId == null) {
                        if (email == null) throw new MessagingException("email도 userId도 없음");
                        userId = userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"))
                                .getUserId();
                    }
                }

                String email = claims.get("email", String.class);
                if (email == null && sub != null && sub.contains("@")) email = sub; // email 보정

                String nickname = claims.get("nickname", String.class); // 없어도 OK(null)

                // 세션 attrs null 방어
                if (accessor.getSessionAttributes() == null) {
                    accessor.setSessionAttributes(new java.util.HashMap<>());
                }

                accessor.getSessionAttributes().put("userId", userId);
                accessor.getSessionAttributes().put("email", email);
                accessor.getSessionAttributes().put("nickname", nickname);

                log.info("[WebSocket 인증 성공] userId: {}, email: {}, nickname: {}", userId, email, nickname);

            } catch (Exception e) {
                log.error("WebSocket 인증 실패", e); // ✅ e.getMessage 말고 스택트레이스!
                throw new MessagingException("JWT 인증 실패");
            }
        }

        if (StompCommand.SEND.equals(accessor.getCommand())) {
            Object userId = accessor.getSessionAttributes() != null
                    ? accessor.getSessionAttributes().get("userId")
                    : null;

            if (userId == null) {
                log.warn("SEND: WebSocket세션에 사용자 정보 없음");
                throw new MessagingException("세션 인증 정보 없음");
            }

            log.info("SEND: userId={} ", userId);
        }

        return message;
    }

}
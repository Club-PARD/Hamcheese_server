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

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                try{
                    jwtUtil.validToken(token); //토큰을 검증
                    //검증에 성공하면
                    String jwt = jwtUtil.substringToken(token); //Bearer를 제거하여 jwt만 뽑고
                    Claims claims = jwtUtil.getClaims(jwt); //jwt에서 Claim을 뽑는다.

                    String email = String.valueOf(claims.getSubject());
                    Long userId = claims.get("userId", Long.class);
                    String userName = userRepository.findById(userId).get().getName();


                    accessor.getSessionAttributes().put("userId", userId);
                    accessor.getSessionAttributes().put("email", email);
                    accessor.getSessionAttributes().put("name", userName);

                    log.info("[WebSocket 인증 성공] userId: {}, email: {}", userId, email);
                } catch (Exception e){
                    log.error("WebSocket 인증 실패 {}", e.getMessage());
                    throw new MessagingException("JWT 인증 실패");
                }
            }
        }
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            Object userId = accessor.getSessionAttributes().get("userId");

            if (userId == null) {
                log.warn("SEND: WebSocket세션에 사용자 정보 없음");
                throw new MessagingException("세션 인증 정보 없음");
            }

            log.info("SEND: userId={} ", userId);
        }
        return message;
    }
}

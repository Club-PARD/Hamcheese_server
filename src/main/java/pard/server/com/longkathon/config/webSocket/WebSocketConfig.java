package pard.server.com.longkathon.config.webSocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker //메세지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompHandler jwtChannelInterceptor;

    public WebSocketConfig(StompHandler jwtChannelInterceptor) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/chat/inbox") //해당 경로로 최초의 핸드셰이크 요청이 들어오도록.
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) { //메세지 발생, 구독 모델의 경로설정
        registry.enableSimpleBroker("/sub"); //해당 경로는 메세지 브로커가 직접 처리
        registry.setApplicationDestinationPrefixes("/pub"); //해당경로는 애플리케이션의 @MessageMapping 메서드와 연결
    }

    @Override//클라이언트가 메세지를 보낼때 거치는 채널 인터셉터를 등록하여 CONNECT, SEND 등의 메시지가 컨트롤러나 브로커로 전달되기 전에 가로채 JWT 인증을 수행합니다
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }
}

package pard.server.com.longkathon.login;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import pard.server.com.longkathon.login.PrincipalOauth2UserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final PrincipalOauth2UserService principalOauth2UserService;
    private final CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain FilterChain(HttpSecurity http) throws Exception{
        // 쿠키 기반 공격 방어 - 토큰 기반이라 꺼두는게 표준
        http.csrf(AbstractHttpConfigurer::disable);

        // 다른 도메인 클라이언트에서 서버에 날리는 요청 처리하는 corsFilter SpringFilters 전에 넣어주는 것.
        http.addFilter(corsConfig.corsFilter());

        // 어떤 url 요청에 대한 인증 (원래 관리자 인증같은걸 여기서 함)
        http.authorizeHttpRequests(au -> au.anyRequest().permitAll());

        // 이 부분이 자동화된 oauth 내부동작을 간단하게 실행시키는 것.
        http.oauth2Login(
                oauth -> oauth
                        .loginPage("/loginForm")
                        .defaultSuccessUrl("/home")
                        .userInfoEndpoint(userInfo -> userInfo.userService(principalOauth2UserService))
        );
        return http.build();
    }
}

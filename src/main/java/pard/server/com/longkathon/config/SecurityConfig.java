package pard.server.com.longkathon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // ⭐ CORS 활성화 (필수)
                .cors(Customizer.withDefaults())

                // CSRF 비활성화 (JWT / idToken 방식이므로)
                .csrf(csrf -> csrf.disable())

                // 세션 사용 안 함
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        // ⭐ preflight 무조건 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Google 로그인 체크 API 허용
                        .requestMatchers("/auth/google/**").permitAll()

                        // 나머지는 일단 다 허용 (개발 단계)
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}

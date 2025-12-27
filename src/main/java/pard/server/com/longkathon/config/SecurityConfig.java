package pard.server.com.longkathon.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/health", "/actuator/health",
                                "/error",
                                "/favicon.ico",
                                "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.svg"
                        ).permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}

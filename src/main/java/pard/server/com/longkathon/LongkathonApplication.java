package pard.server.com.longkathon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing //BaseEntity에서 생성시간, 엡뎃 시간 유지
@SpringBootApplication
public class LongkathonApplication {

    public static void main(String[] args) {
        SpringApplication.run(LongkathonApplication.class, args);
    }

}

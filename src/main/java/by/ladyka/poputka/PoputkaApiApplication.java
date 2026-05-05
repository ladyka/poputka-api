package by.ladyka.poputka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Instant;
import java.time.ZoneId;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
class PoputkaApiApplication {

    public static void main(String[] args) {
        long b = 1700000000000L;
        for (int i = 0; i <30; i++) {
            b += 100000000000L;
            System.out.println(b + " " + Instant.ofEpochMilli(b).atZone(ZoneId.systemDefault()));
        }
        SpringApplication.run(PoputkaApiApplication.class, args);
    }

}

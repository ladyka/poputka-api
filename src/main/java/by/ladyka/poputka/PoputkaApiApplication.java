package by.ladyka.poputka;

import by.ladyka.poputka.auth.AuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableConfigurationProperties(AuthProperties.class)
@EnableJpaAuditing
@EnableJpaRepositories
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@EnableMethodSecurity
class PoputkaApiApplication {

    static void main(String[] args) {
        SpringApplication.run(PoputkaApiApplication.class, args);
    }
}

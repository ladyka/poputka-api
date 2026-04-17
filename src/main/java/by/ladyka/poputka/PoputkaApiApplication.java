package by.ladyka.poputka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories
@EnableScheduling
class PoputkaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoputkaApiApplication.class, args);
    }

}

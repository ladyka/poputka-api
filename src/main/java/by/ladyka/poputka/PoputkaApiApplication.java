package by.ladyka.poputka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
class PoputkaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoputkaApiApplication.class, args);
    }

}

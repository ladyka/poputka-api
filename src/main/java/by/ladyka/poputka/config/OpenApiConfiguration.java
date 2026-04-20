package by.ladyka.poputka.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI poputkaOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Poputka API")
                .version("0.0.4")
                .description("Poputka HTTP API"));
    }
}



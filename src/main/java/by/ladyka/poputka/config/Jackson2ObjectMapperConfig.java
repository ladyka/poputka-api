package by.ladyka.poputka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static by.ladyka.poputka.config.StringSanitizationConfiguration.registerTrimmingModule;

// Boot 4 registers Jackson 3 for HTTP; services using com.fasterxml.databind need their own mapper.
@Configuration
class Jackson2ObjectMapperConfig {

    @Bean
    ObjectMapper jackson2ObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        registerTrimmingModule(mapper);
        return mapper;
    }
}

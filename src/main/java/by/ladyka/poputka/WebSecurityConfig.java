package by.ladyka.poputka;

import by.ladyka.poputka.controllers.TelegramController;
import by.ladyka.poputka.controllers.TripController;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final PoputkaUserRepository poputkaUserRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> requests
                                // public trip endpoints (read/search)
                                .requestMatchers(HttpMethod.GET, TripController.API_TRIP + "/**").permitAll()
                                .requestMatchers(HttpMethod.POST, TripController.API_TRIP + "/search").permitAll()
                                .requestMatchers(HttpMethod.GET, TripController.API_TRIP + "/popular").permitAll()

                                // write trip endpoints (auth required)
                                .requestMatchers(HttpMethod.POST, TripController.API_TRIP + "/").authenticated()

                                .requestMatchers(
                                        TelegramController.API_TELEGRAM,
                                        "/api/user/signup",
                                        "/api/user/info",
                                        "/api/user/currentAuth",
                                        "/api/search",
                                        "/api/search/",
                                        "/api/search/**",
                                        "/actuator",
                                        "/actuator/",
                                        "/actuator/**"
                                                ).permitAll()
                                .anyRequest().authenticated()
                                      )
                .formLogin(Customizer.withDefaults())
                .logout(LogoutConfigurer::permitAll);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            log.info(username);
            PoputkaUser poputkaUser = poputkaUserRepository.findByUsername(username).orElseThrow(
                    () -> new UsernameNotFoundException(username));
            return new ApplicationUserDetails(poputkaUser);
        };
    }

    @Bean
    public AuditorAware<PoputkaUser> auditorAware() {
        return () -> Objects.requireNonNull(Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .map(poputkaUserRepository::findByUsername)
                .orElseThrow());
    }

    @Profile("!local")
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Profile("local")
    @Bean
    public PasswordEncoder passwordEncoderLocal() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return (String) rawPassword;
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return Objects.equals(rawPassword, encodedPassword);
            }
        };
    }
}

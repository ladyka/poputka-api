package by.ladyka.poputka;

import by.ladyka.poputka.auth.JwtAuthenticationFilter;
import by.ladyka.poputka.auth.JwtTokenService;
import by.ladyka.poputka.controllers.TelegramController;
import by.ladyka.poputka.controllers.TripController;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final PoputkaUserRepository poputkaUserRepository;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService jwtTokenService, UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtTokenService, userDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((requests) -> requests
                                // driver's own trips — must be authenticated (before public GET trip/** wildcard)
                                .requestMatchers(HttpMethod.GET, TripController.API_TRIP + "/owned").authenticated()
                                // public trip endpoints (read/search)
                                .requestMatchers(HttpMethod.GET, TripController.API_TRIP + "/**").permitAll()
                                .requestMatchers(HttpMethod.POST, TripController.API_TRIP + "/search").permitAll()
                                .requestMatchers(HttpMethod.GET, TripController.API_TRIP + "/popular").permitAll()

                                // write trip endpoints (auth required)
                                .requestMatchers(HttpMethod.POST, TripController.API_TRIP + "/").authenticated()

                                .requestMatchers(
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html")
                                .permitAll()
                                .requestMatchers(
                                        "/api/auth/login",
                                        "/api/auth/google",
                                        "/api/auth/apple",
                                        "/api/auth/refresh",
                                        "/api/auth/logout")
                                        .permitAll()
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
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(
                                (rq, rp, ex) -> {
                                    rp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    rp.setContentType("application/json;charset=UTF-8");
                                    rp.getWriter().write("{\"error\":\"unauthorized\"}");
                                })
                        .accessDeniedHandler((rq, rp, ex) -> {
                            rp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            rp.setContentType("application/json;charset=UTF-8");
                            rp.getWriter().write("{\"error\":\"forbidden\"}");
                        }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }
            // Avoid repository lookup during JPA flush: findByUsername can auto-flush and recurse into auditing.
            if (authentication.getPrincipal() instanceof ApplicationUserDetails details) {
                return Optional.of(details.user());
            }
            return poputkaUserRepository.findByUsername(authentication.getName());
        };
    }

    @Profile("prod")
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Profile("!prod")
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

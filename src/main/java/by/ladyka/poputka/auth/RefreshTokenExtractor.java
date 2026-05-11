package by.ladyka.poputka.auth;

import by.ladyka.poputka.auth.dto.RefreshRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenExtractor {

    private final AuthProperties authProperties;

    /** Prefers explicit JSON refresh token when present (mobile); otherwise reads HttpOnly cookie. */
    public Optional<String> extract(RefreshRequest body, HttpServletRequest request) {
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            return Optional.of(body.refreshToken());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie c : cookies) {
            if (authProperties.getRefreshCookieName().equals(c.getName())
                    && c.getValue() != null
                    && !c.getValue().isBlank()) {
                return Optional.of(c.getValue());
            }
        }
        return Optional.empty();
    }
}

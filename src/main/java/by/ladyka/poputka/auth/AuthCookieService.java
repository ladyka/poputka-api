package by.ladyka.poputka.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthCookieService {

    private final AuthProperties authProperties;
    private final Environment environment;

    public ResponseCookie buildRefreshCookie(String refreshTokenPlain) {
        Duration maxAge = Duration.ofDays(authProperties.getRefreshTokenValidityDays());
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(
                        authProperties.getRefreshCookieName(), refreshTokenPlain)
                .httpOnly(true)
                .path(authProperties.getRefreshCookiePath())
                .sameSite("Lax")
                .maxAge(maxAge);
        if (environment.matchesProfiles("prod")) {
            b = b.secure(true);
        }
        return b.build();
    }

    public ResponseCookie clearRefreshCookie() {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(authProperties.getRefreshCookieName(), "")
                .httpOnly(true)
                .path(authProperties.getRefreshCookiePath())
                .maxAge(Duration.ZERO)
                .sameSite("Lax");
        if (environment.matchesProfiles("prod")) {
            b = b.secure(true);
        }
        return b.build();
    }
}

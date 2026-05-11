package by.ladyka.poputka.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Validates Apple «Sign in with Apple» identity tokens (JWKS signature, issuer, audience, expiry).
 * <p>
 * Audience = Services ID (web) или Bundle ID (iOS) из Apple Developer — задаётся в {@code poputka.auth.apple-audience}.
 */
@Service
@RequiredArgsConstructor
public class AppleTokenVerificationService {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_JWKS_URI = "https://appleid.apple.com/auth/keys";

    private final AuthProperties authProperties;

    private volatile NimbusJwtDecoder decoder;

    private NimbusJwtDecoder getOrBuildDecoder() {
        if (decoder != null) {
            return decoder;
        }
        synchronized (this) {
            if (decoder != null) {
                return decoder;
            }
            String audience = authProperties.getAppleAudience();
            if (audience == null || audience.isBlank()) {
                return null;
            }
            NimbusJwtDecoder d = NimbusJwtDecoder.withJwkSetUri(APPLE_JWKS_URI).build();
            OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(APPLE_ISSUER);
            OAuth2TokenValidator<Jwt> withAudience =
                    new JwtClaimValidator<Object>("aud", audVal -> audienceContains(audVal, audience));
            d.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
            this.decoder = d;
            return decoder;
        }
    }

    private static boolean audienceContains(Object audClaim, String configuredAudience) {
        if (audClaim instanceof String s) {
            return configuredAudience.equals(s);
        }
        if (audClaim instanceof Collection<?> c) {
            return c.contains(configuredAudience);
        }
        return false;
    }

    public AppleUserPrincipal verify(String identityToken) {
        NimbusJwtDecoder dec = getOrBuildDecoder();
        if (dec == null) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Sign in with Apple is not configured");
        }
        try {
            Jwt jwt = dec.decode(identityToken);
            String sub = jwt.getSubject();
            String email = jwt.getClaimAsString("email");
            boolean emailVerified = parseEmailVerifiedClaim(jwt.getClaim("email_verified"));
            return new AppleUserPrincipal(sub, email, emailVerified);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid Apple identity token", e);
        }
    }

    /** Apple может отдавать {@code email_verified} как {@link Boolean} или строку {@code "true"|"false"}. */
    private static boolean parseEmailVerifiedClaim(Object claim) {
        if (claim instanceof Boolean b) {
            return b;
        }
        if (claim instanceof String s) {
            return "true".equalsIgnoreCase(s);
        }
        return false;
    }
}

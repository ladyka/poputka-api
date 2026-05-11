package by.ladyka.poputka.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Validates Google OpenID Connect ID tokens (signature, issuer, audience, expiry).
 */
@Service
@RequiredArgsConstructor
public class GoogleTokenVerificationService {

    private static final String GOOGLE_ISSUER = "https://accounts.google.com";

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
            String clientId = authProperties.getGoogleClientId();
            if (clientId == null || clientId.isBlank()) {
                return null;
            }
            NimbusJwtDecoder d = JwtDecoders.fromIssuerLocation(GOOGLE_ISSUER);
            OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(GOOGLE_ISSUER);
            OAuth2TokenValidator<Jwt> withAudience =
                    new JwtClaimValidator<Object>("aud", aud -> audienceContainsClientId(aud, clientId));
            d.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
            this.decoder = d;
            return decoder;
        }
    }

    private static boolean audienceContainsClientId(Object audClaim, String clientId) {
        if (audClaim instanceof String s) {
            return clientId.equals(s);
        }
        if (audClaim instanceof Collection<?> c) {
            return c.contains(clientId);
        }
        return false;
    }

    public GoogleUserPrincipal verify(String idToken) {
        NimbusJwtDecoder dec = getOrBuildDecoder();
        if (dec == null) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Google Sign-In is not configured");
        }
        try {
            Jwt jwt = dec.decode(idToken);
            String sub = jwt.getSubject();
            String email = jwt.getClaimAsString("email");
            if (email == null || email.isBlank()) {
                throw new ResponseStatusException(UNAUTHORIZED, "Google token has no email");
            }
            Object ev = jwt.getClaim("email_verified");
            boolean emailVerified = ev instanceof Boolean b && b;
            return new GoogleUserPrincipal(sub, email, emailVerified);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid Google ID token", e);
        }
    }
}

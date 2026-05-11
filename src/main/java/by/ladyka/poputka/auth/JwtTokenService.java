package by.ladyka.poputka.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private static final String CLAIM_UID = "uid";

    private final AuthProperties authProperties;
    private SecretKey accessKey;

    @PostConstruct
    void init() {
        String secret = authProperties.getJwtSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("poputka.auth.jwt-secret is required");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            try {
                keyBytes = Decoders.BASE64.decode(secret.trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(
                        "poputka.auth.jwt-secret must be at least 32 UTF-8 bytes (or Base64 for 32+ raw bytes)", e);
            }
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("poputka.auth.jwt-secret must provide at least 256 bits for HS256");
        }
        this.accessKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(String username, long userId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(authProperties.getAccessTokenValidityMinutes() * 60L);
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_UID, userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(accessKey)
                .compact();
    }

    /**
     * Validates signature and expiry; returns bearer username (email).
     */
    public Claims parseAccessToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(accessKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid access token", e);
        }
    }
}

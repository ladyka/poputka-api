package by.ladyka.poputka.auth;

import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.RefreshToken;
import by.ladyka.poputka.data.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthProperties authProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Persists hashed refresh token; returns plaintext for the client / cookie (never stored).
     */
    @Transactional
    public String issue(PoputkaUser user, String userAgent, String ipAddress) {
        Instant now = Instant.now();
        Instant expires = now.plus(authProperties.getRefreshTokenValidityDays(), ChronoUnit.DAYS);
        String plain = generateRawToken();
        String hash = sha256Hex(plain);
        RefreshToken row = new RefreshToken();
        row.setId(UUID.randomUUID().toString());
        row.setUser(user);
        row.setTokenHash(hash);
        row.setExpiresAt(expires);
        row.setRevokedAt(null);
        row.setCreatedAt(now);
        row.setUserAgent(truncate(userAgent, 512));
        row.setIpAddress(truncate(ipAddress, 64));
        refreshTokenRepository.save(row);
        return plain;
    }

    /**
     * Revokes the presented token and mints a new one (rotation).
     *
     * @return user (for new access JWT) and new plaintext refresh token
     */
    @Transactional
    public RotatedRefresh rotateReturningUser(String plaintextRefresh, String userAgent, String ipAddress) {
        if (plaintextRefresh == null || plaintextRefresh.isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Missing refresh token");
        }
        String hash = sha256Hex(plaintextRefresh);
        RefreshToken existing =
                refreshTokenRepository.findByTokenHash(hash).orElseThrow(() -> new ResponseStatusException(
                        UNAUTHORIZED, "Unknown refresh token"));
        Instant now = Instant.now();
        if (!existing.isActive(now)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Refresh token expired or revoked");
        }
        PoputkaUser user = existing.getUser();
        existing.setRevokedAt(now);
        refreshTokenRepository.save(existing);
        String nextPlain = issue(user, userAgent, ipAddress);
        return new RotatedRefresh(user, nextPlain);
    }

    public record RotatedRefresh(PoputkaUser user, String refreshPlain) {
    }

    @Transactional
    public void revokeByPlaintext(String plaintextRefresh) {
        if (plaintextRefresh == null || plaintextRefresh.isBlank()) {
            return;
        }
        String hash = sha256Hex(plaintextRefresh);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(existing -> {
            existing.setRevokedAt(Instant.now());
            refreshTokenRepository.save(existing);
        });
    }

    private String generateRawToken() {
        byte[] buf = new byte[32];
        secureRandom.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    private static String sha256Hex(String plain) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(plain.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }
}

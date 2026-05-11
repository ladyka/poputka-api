package by.ladyka.poputka.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Auth token policy and transport defaults.
 * <p>
 * Refresh tokens are delivered in two ways (same raw value):
 * <ul>
 *   <li><b>HttpOnly cookie</b> (default path {@link #refreshCookiePath}) for same-site SPAs — mitigates XSS reading the refresh token.</li>
 *   <li><b>JSON field</b> {@code refreshToken} in auth responses for native/mobile clients that use secure OS storage.</li>
 * </ul>
 * Access JWT is always short-lived and returned only in JSON; clients should keep it in memory.
 */
@ConfigurationProperties(prefix = "poputka.auth")
@Getter
@Setter
public class AuthProperties {

    /**
     * HMAC SHA key for signing access JWTs ({@literal >=} 256 bits recommended).
     */
    private String jwtSecret;

    /** Access JWT lifetime */
    private int accessTokenValidityMinutes = 15;

    /** Stored refresh token lifetime (rotation mints a new expiry from refresh time). */
    private int refreshTokenValidityDays = 30;

    /** Google OAuth 2 client id — audience for ID tokens */
    private String googleClientId = "";

    /**
     * Apple audience for identity tokens — <b>Services ID</b> (web / JS) или <b>Bundle ID</b> (нативные приложения).
     */
    private String appleAudience = "";

    private String refreshCookieName = "refreshToken";

    /** Narrow cookie scope to refresh/logout/login endpoints */
    private String refreshCookiePath = "/api/auth";

}

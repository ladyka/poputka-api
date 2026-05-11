package by.ladyka.poputka.auth;

public final class JwtBearerConstants {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    /** Include trailing space — value is prefixed to the raw JWT. */
    public static final String BEARER_PREFIX = "Bearer ";

    private JwtBearerConstants() {}
}

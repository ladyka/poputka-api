package by.ladyka.poputka.auth;

/**
 * Verified Google ID token identity.
 */
public record GoogleUserPrincipal(String sub, String email, boolean emailVerified) {
}

package by.ladyka.poputka.auth;

import java.util.Objects;

/** Verified Apple identity token ({@link AppleTokenVerificationService}). */
public record AppleUserPrincipal(String sub, String email, boolean emailVerified) {

    public AppleUserPrincipal {
        Objects.requireNonNull(sub, "sub");
    }

    /** {@code email} может быть {@code null} — Apple отдаёт его только при первых входах или «Share My Email». */
    public static boolean emailPresent(String email) {
        return email != null && !email.isBlank();
    }
}

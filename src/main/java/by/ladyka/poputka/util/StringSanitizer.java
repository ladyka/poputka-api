package by.ladyka.poputka.util;

/**
 * Normalizes user-supplied strings by trimming invisible whitespace.
 *
 * <p>We intentionally only trim at the edges. Collapsing internal whitespace is application-specific.</p>
 */
public final class StringSanitizer {

    private StringSanitizer() {
    }

    public static String trimInvisible(String raw) {
        if (raw == null) {
            return null;
        }
        if (raw.isEmpty()) {
            return raw;
        }

        // Common "invisible" characters that users paste from messengers.
        String normalized = raw
                .replace('\u00A0', ' ')  // NO-BREAK SPACE
                .replace('\u2007', ' ')  // FIGURE SPACE
                .replace('\u202F', ' ')  // NARROW NO-BREAK SPACE
                // Zero-width characters often appear in copied emails/usernames.
                .replace("\u200B", "")   // ZERO WIDTH SPACE
                .replace("\u200C", "")   // ZERO WIDTH NON-JOINER
                .replace("\u200D", "")   // ZERO WIDTH JOINER
                .replace("\u2060", "")   // WORD JOINER
                .replace("\uFEFF", "");  // ZERO WIDTH NO-BREAK SPACE (BOM)

        return normalized.strip();
    }
}


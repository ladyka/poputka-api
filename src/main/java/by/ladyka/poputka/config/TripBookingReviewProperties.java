package by.ladyka.poputka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@ConfigurationProperties(prefix = "poputka.trip-booking-review")
public class TripBookingReviewProperties {

    /**
     * After this duration from review creation, a {@code DRAFT} is moved to {@code PENDING_MODERATION} automatically.
     */
    private Duration draftToModerationDelay = Duration.ofHours(1);

    /**
     * Usernames allowed to call moderation endpoints. Empty means no user may moderate.
     */
    private Set<String> moderatorUsernames = new LinkedHashSet<>();
}

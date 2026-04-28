package by.ladyka.poputka.data.dto.bookingTripOverview;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

/** Driver aggregate seat summary plus all matching booking rows (unpaged {@link Page}). */
@Getter
@Builder
public class TripBookingOverviewResponseDto {
    private final TripBookingOverviewSummaryDto summary;
    /** Full list under {@code bookings.content}; metadata comes from Spring Data {@link Page} (unpaged). */
    private final Page<TripBookingOverviewItemDto> bookings;
}

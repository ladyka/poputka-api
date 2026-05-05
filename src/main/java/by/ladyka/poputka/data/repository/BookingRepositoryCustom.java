package by.ladyka.poputka.data.repository;

import by.ladyka.poputka.data.enums.BookingOverviewScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Paginated driver's trip-booking overview rows (native query with last-message preview).
 */
public interface BookingRepositoryCustom {

    /**
     * One row per booking: id, bookingStatus string, passenger display name, last payload JSON text,
     * message_status string or null, last_message_created millis or null (Long).
     */
    Page<Object[]> pageTripBookingOverview(Long tripId, BookingOverviewScope scope, long nowMillis, Pageable pageable);
}

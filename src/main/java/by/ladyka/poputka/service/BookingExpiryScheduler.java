package by.ladyka.poputka.service;

import by.ladyka.poputka.data.enums.BookingStatus;
import by.ladyka.poputka.data.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class BookingExpiryScheduler {
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    /**
     * Expire bookings that are still WAITING after the trip start time.
     */
    @Scheduled(cron = "${poputka.booking-expiry.cron:0 0 * * * *}")
    public void expireWaitingBookingsPastTripStart() {
        long nowMillis = Instant.now().toEpochMilli();
        for (String bookingId : bookingRepository.findIdsByBookingStatusAndTripStartLessThanEqual(
                BookingStatus.WAITING.name(),
                nowMillis
        )) {
            bookingService.systemExpireWaitingBooking(bookingId);
        }
    }
}

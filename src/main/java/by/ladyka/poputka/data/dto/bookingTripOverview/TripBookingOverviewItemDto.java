package by.ladyka.poputka.data.dto.bookingTripOverview;

import by.ladyka.poputka.data.dto.payload.MessagePayload;
import by.ladyka.poputka.data.enums.BookingStatus;
import by.ladyka.poputka.data.enums.MessageStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class TripBookingOverviewItemDto {
    private final String bookingId;
    private final BookingStatus bookingStatus;
    /** Passenger full name only (no login or Telegram fields). */
    private final String passengerDisplayName;
    private final MessagePayload lastMessagePayload;
    private final MessageStatus messageStatus;
    private final Instant lastMessageTime;
}

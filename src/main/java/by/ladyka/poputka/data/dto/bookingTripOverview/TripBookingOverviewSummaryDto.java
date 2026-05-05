package by.ladyka.poputka.data.dto.bookingTripOverview;

import lombok.Builder;
import lombok.Getter;

/** Seat summary for driver's trip-booking overview; independent of paging / {@code bookingScope}. */
@Getter
@Builder
public class TripBookingOverviewSummaryDto {
    private final long tripId;
    private final int passengerSeatCapacity;
    private final int occupiedSeats;
    private final int freePassengerSeats;
    private final boolean full;
}

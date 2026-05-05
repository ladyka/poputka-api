package by.ladyka.poputka.data.enums;

import java.util.EnumSet;
import java.util.List;

public enum BookingStatus {
    WAITING,
    ACCEPTED,
    REJECTED,
    CANCELLED,
    EXPIRED,
    NO_SHOW,
    CHECKED_IN,
    IN_PROGRESS,
    COMPLETED;

    /** Bookings that count toward trip passenger-seat capacity ({@code trips.passengers}). */
    private static final EnumSet<BookingStatus> OCCUPYING_PASSENGER_SEAT_STATUSES =
            EnumSet.of(ACCEPTED, CHECKED_IN, IN_PROGRESS);

    /** Terminal rows for driver's trip-booking overview filter ({@code bookingScope}). */
    private static final EnumSet<BookingStatus> TERMINAL_FOR_OVERVIEW_STATUSES =
            EnumSet.of(COMPLETED, REJECTED, CANCELLED, EXPIRED, NO_SHOW);

    public boolean occupiesPassengerSeat() {
        return OCCUPYING_PASSENGER_SEAT_STATUSES.contains(this);
    }

    public boolean terminalForTripOverview() {
        return TERMINAL_FOR_OVERVIEW_STATUSES.contains(this);
    }

    /** For Spring Data {@code countByTripIdAndBookingStatusIn}. */
    public static List<BookingStatus> occupyingPassengerSeatStatuses() {
        return List.copyOf(OCCUPYING_PASSENGER_SEAT_STATUSES);
    }
}

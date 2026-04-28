package by.ladyka.poputka.data.enums;

/** Filter for driver's trip booking overview ({@code GET /api/booking/trip/{tripId}/overview}). */
public enum BookingOverviewScope {

    /** No filter — all bookings for the trip appear in the paged list. */
    ALL,

    /**
     * {@code trip.start >= now} and booking status not terminal ({@link BookingStatus#terminalForTripOverview()}).
     */
    ACTIVE,

    /**
     * {@code trip.start < now} or booking status is terminal ({@link BookingStatus#terminalForTripOverview()}).
     */
    ARCHIVED;

    /** Accepted query values: {@code all}, {@code active}, {@code archived}; blank defaults to {@link #ALL}. */
    public static BookingOverviewScope parseQueryParameter(String raw) {
        if (raw == null || raw.isBlank()) {
            return ALL;
        }
        return BookingOverviewScope.valueOf(raw.trim().toUpperCase());
    }
}

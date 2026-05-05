package by.ladyka.poputka.data.repository;

/**
 * Native SQL for paginated driver's trip-booking overview.
 *
 * <p>Terminal status literals must match {@link by.ladyka.poputka.data.enums.BookingStatus}</p>
 */
public final class TripBookingOverviewSqlFragments {

    private static final String TERMINAL_STATUSES_LIST =
            "( 'COMPLETED', 'REJECTED', 'CANCELLED', 'EXPIRED', 'NO_SHOW' )";

    /**
     * Scope filter (combined rule C): active / archived / all. Appended after {@code WHERE b.trip_id = :tripId}.
     */
    private static final String SCOPE_PREDICATE =
            """
                             AND (
                                 CAST(:scopeAll AS boolean) IS TRUE
                                 OR (
                                             CAST(:scopeActive AS boolean) IS TRUE
                                             AND t.start >= :nowMillis
                                             AND COALESCE(NULLIF(TRIM(BOTH FROM CAST(b.booking_status AS VARCHAR)), ''), 'WAITING')
                                                 NOT IN """
                    + TERMINAL_STATUSES_LIST
                    + """
                                         )
                                 OR (
                                             CAST(:scopeArchived AS boolean) IS TRUE
                                             AND (
                                                         t.start < :nowMillis
                                                         OR COALESCE(NULLIF(TRIM(BOTH FROM CAST(b.booking_status AS VARCHAR)), ''), 'WAITING')
                                                             IN """
                    + TERMINAL_STATUSES_LIST
                    + """
                                                     )
                                             )
                                     )"""

                    ;

    /**
     * Rows per booking plus last-message preview (no paging — applied in {@link by.ladyka.poputka.data.repository.impl.BookingRepositoryImpl}).
     */
    public static final String SELECT_OVERVIEW_BOOKINGS_SQL =
            """
                    SELECT b.id,
                           CAST(b.booking_status AS VARCHAR) AS booking_status,
                           TRIM(REGEXP_REPLACE(concat_ws(' ', u.name, u.surname), '[[:space:]]+', ' ', 'g')) AS passenger_display_name,
                           lm.payload_text AS last_payload_text,
                           CAST(lm.message_status AS VARCHAR) AS message_status_text,
                           lm.last_message_created
                    FROM booking b
                             INNER JOIN trips t ON t.id = b.trip_id
                             INNER JOIN users u ON u.id = b.passenger_id
                             LEFT JOIN (
                        SELECT DISTINCT ON (m.booking_id) m.booking_id,
                                                         CAST(m.payload AS text) AS payload_text,
                                                         m.message_status,
                                                         m.created_datetime AS last_message_created
                        FROM message m
                        ORDER BY m.booking_id, m.created_datetime DESC NULLS LAST
                    ) lm ON lm.booking_id = b.id
                    WHERE b.trip_id = :tripId

                    """
                    + SCOPE_PREDICATE;

    /** Same scope as data query, without message join (cheaper and avoids duplicate-row count issues). */
    public static final String COUNT_OVERVIEW_BOOKINGS_SQL =
            """
                    SELECT COUNT(b.id)
                    FROM booking b
                             INNER JOIN trips t ON t.id = b.trip_id
                    WHERE b.trip_id = :tripId

                    """
                    + SCOPE_PREDICATE;

    private TripBookingOverviewSqlFragments() {
    }
}

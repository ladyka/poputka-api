package by.ladyka.poputka.data.repository;

import by.ladyka.poputka.data.entity.Booking;
import by.ladyka.poputka.data.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    Integer countByTripIdAndBookingStatus(long tripId, BookingStatus bookingStatus);

    List<Booking> findBookingByTripId(Long tripId);

    Optional<Booking> findBookingByTripIdAndPassengerId(Long tripId, Long passengerId);

    @Query(value = """
                       WITH driver_bookings AS (
                           SELECT
                               b.id AS booking_id,
                               b.trip_id,
                               t.place_from,
                               t.place_to,
                               t.start,
                               b.booking_status,
                               concat(up.name , ' ' , up.surname) AS opposite_user_name,
                               m.content,
                               m.message_status,
                               m.created_datetime AS last_message_time,
                               'driver' AS user_role,
                               ROW_NUMBER() OVER (PARTITION BY b.id ORDER BY m.created_datetime DESC) AS msg_rank
                           FROM trips t
                           JOIN booking b ON t.id = b.trip_id
                           JOIN users up ON b.passenger_id = up.id
                           LEFT JOIN message m ON m.booking_id = b.id
                           WHERE t.owner_id = :user_id
                       ),
                       passenger_bookings AS (
                           SELECT
                               b.id AS booking_id,
                               b.trip_id,
                               t.place_from,
                               t.place_to,
                               t.start,
                               b.booking_status,
                               concat(uo.name , ' ' , uo.surname) AS opposite_user_name,
                               m.content,
                               m.message_status,
                               m.created_datetime AS last_message_time,
                               'passenger' AS user_role,
                               ROW_NUMBER() OVER (PARTITION BY b.id ORDER BY m.created_datetime DESC) AS msg_rank
                           FROM booking b
                           JOIN trips t ON b.trip_id = t.id
                           JOIN users uo ON t.owner_id = uo.id
                           LEFT JOIN message m ON m.booking_id = b.id
                           WHERE b.passenger_id = :user_id
                       ),
                       combined AS (
                           SELECT
                               booking_id,
                               trip_id,
                               place_from,
                               place_to,
                               start,
                               booking_status,
                               opposite_user_name,
                               content,
                               message_status,
                               last_message_time,
                               user_role
                           FROM driver_bookings
                           WHERE msg_rank = 1 OR msg_rank IS NULL
                   
                           UNION ALL
                   
                           SELECT
                               booking_id,
                               trip_id,
                               place_from,
                               place_to,
                               start,
                               booking_status,
                               opposite_user_name,
                               content,
                               message_status,
                               last_message_time,
                               user_role
                           FROM passenger_bookings
                           WHERE msg_rank = 1 OR msg_rank IS NULL
                       )
                       SELECT *
                       FROM combined
                       ORDER BY last_message_time DESC NULLS LAST;
                   """, nativeQuery = true)
        //    List<BookingMessageDto> findBookingByUserId(Long user_id);
    List<Object[]> findBookingByUserId(Long user_id);
}

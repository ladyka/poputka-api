package by.ladyka.poputka.data.repository;

import by.ladyka.poputka.data.entity.Booking;
import by.ladyka.poputka.data.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    Integer countByTripIdAndBookingStatus(long tripId, BookingStatus bookingStatus);

    List<Booking> findBookingByTripId(Long tripId);
}

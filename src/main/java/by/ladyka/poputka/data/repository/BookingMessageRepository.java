package by.ladyka.poputka.data.repository;

import by.ladyka.poputka.data.entity.BookingMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingMessageRepository extends JpaRepository<BookingMessage, String> {
    List<BookingMessage> findByBookingId(String bookingId);
}

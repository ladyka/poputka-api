package by.ladyka.poputka.data.dto;

import by.ladyka.poputka.data.entity.Booking;
import by.ladyka.poputka.data.enums.BookingStatus;
import lombok.Data;

/**
 * DTO for {@link Booking}
 */
@Data
public class BookingDto {
    private String id;
    private Long tripId;
    private Long passengerId;
    private BookingStatus bookingStatus;
}

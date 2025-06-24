package by.ladyka.poputka.data.dto;

import by.ladyka.poputka.data.enums.BookingStatus;
import by.ladyka.poputka.data.enums.MessageStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class BookingMessageDto {
    private String bookingId;
    private long tripId;
    private String placeFrom;
    private String placeTo;
    private Instant start;
    private BookingStatus bookingStatus;
    private String oppositeUserName;
    private String content;
    private MessageStatus messageStatus;
    private Instant lastMessageTime;
    private String userRole;
}

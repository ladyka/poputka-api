package by.ladyka.poputka.data.dto;

import by.ladyka.poputka.data.enums.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BookingAvailableStatusesResponseDto {
    private List<BookingStatus> available;
}

package by.ladyka.poputka.data.dto;

import by.ladyka.poputka.data.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingStatusChangeRequestDto {
    @NotNull
    private BookingStatus to;
}

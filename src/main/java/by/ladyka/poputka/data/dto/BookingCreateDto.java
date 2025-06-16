package by.ladyka.poputka.data.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BookingCreateDto {
    @NotNull
    @Positive
    private Long tripId;
    private String message;
}

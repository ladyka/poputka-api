package by.ladyka.poputka.data.dto.tripbookingreview;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TripBookingReviewCreateRequestDto {

    @Min(1)
    @Max(5)
    private int rating;

    @Size(max = 2000)
    private String comment;
}

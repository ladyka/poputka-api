package by.ladyka.poputka.data.dto.tripbookingreview;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TripBookingReviewModerationRequestDto {

    @NotNull
    private TripBookingReviewModerationDecision decision;

    @Size(max = 2000)
    private String moderatorComment;
}

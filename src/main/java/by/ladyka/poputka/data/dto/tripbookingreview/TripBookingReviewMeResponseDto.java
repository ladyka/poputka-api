package by.ladyka.poputka.data.dto.tripbookingreview;

import lombok.Data;

@Data
public class TripBookingReviewMeResponseDto {
    private boolean hasReview;
    private TripBookingReviewItemDto review;
}

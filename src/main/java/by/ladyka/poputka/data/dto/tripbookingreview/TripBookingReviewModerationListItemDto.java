package by.ladyka.poputka.data.dto.tripbookingreview;

import by.ladyka.poputka.data.enums.TripBookingReviewStatus;
import lombok.Data;

@Data
public class TripBookingReviewModerationListItemDto {
    private Long id;
    private String bookingId;
    private Long reviewerId;
    private String reviewerUsername;
    private Long revieweeId;
    private TripBookingReviewStatus status;
    private Integer rating;
    private String comment;
    private Long createdAt;
}

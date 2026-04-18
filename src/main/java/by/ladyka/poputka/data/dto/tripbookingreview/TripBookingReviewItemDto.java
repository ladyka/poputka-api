package by.ladyka.poputka.data.dto.tripbookingreview;

import by.ladyka.poputka.data.enums.TripBookingReviewStatus;
import lombok.Data;

@Data
public class TripBookingReviewItemDto {
    private Long id;
    private String bookingId;
    private TripBookingReviewStatus status;
    private Integer rating;
    private String comment;
    private Long revieweeId;
    private Long createdAt;
    private boolean canEdit;
    private Long editableUntil;
    private String moderatorComment;
}

package by.ladyka.poputka.data.dto.tripbookingreview;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripBookingReviewPublicListItemDto {
    private Long id;
    private Integer rating;
    private String comment;
    private Long approvedAt;
    private String tripPlaceFrom;
    private String tripPlaceTo;
}

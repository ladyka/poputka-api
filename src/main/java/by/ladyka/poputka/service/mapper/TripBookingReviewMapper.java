package by.ladyka.poputka.service.mapper;

import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewItemDto;
import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewModerationListItemDto;
import by.ladyka.poputka.data.entity.TripBookingReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripBookingReviewMapper {

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "revieweeId", source = "reviewee.id")
    @Mapping(target = "createdAt", source = "createdDatetime")
    @Mapping(target = "canEdit", constant = "false")
    @Mapping(target = "editableUntil", ignore = true)
    TripBookingReviewItemDto toItemDto(TripBookingReview review);

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "reviewerId", source = "createdUser.id")
    @Mapping(target = "reviewerUsername", source = "createdUser.username")
    @Mapping(target = "revieweeId", source = "reviewee.id")
    @Mapping(target = "createdAt", source = "createdDatetime")
    TripBookingReviewModerationListItemDto toModerationListItem(TripBookingReview review);
}

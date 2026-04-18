package by.ladyka.poputka.service;

import by.ladyka.poputka.config.TripBookingReviewProperties;
import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewCreateRequestDto;
import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewItemDto;
import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewMeResponseDto;
import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewModerationDecision;
import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewModerationListItemDto;
import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewModerationRequestDto;
import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewPatchRequestDto;
import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewPublicListItemDto;
import by.ladyka.poputka.data.entity.Booking;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripBookingReview;
import by.ladyka.poputka.data.entity.TripEntity;
import by.ladyka.poputka.data.enums.BookingStatus;
import by.ladyka.poputka.data.enums.TripBookingReviewStatus;
import by.ladyka.poputka.data.repository.BookingRepository;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import by.ladyka.poputka.data.repository.TripBookingReviewRepository;
import by.ladyka.poputka.data.repository.TripRepository;
import by.ladyka.poputka.service.mapper.TripBookingReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripBookingReviewService {

    private final TripBookingReviewRepository tripBookingReviewRepository;
    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final PoputkaUserRepository poputkaUserRepository;
    private final TripBookingReviewProperties properties;
    private final TripBookingReviewMapper tripBookingReviewMapper;
    private final TripTripRatingService tripTripRatingService;

    public TripBookingReviewMeResponseDto getMyReview(String username, String bookingId) {
        PoputkaUser user = requireUser(username);
        BookingTripContext ctx = loadBookingContext(bookingId);
        assertParticipantOrNotFound(user, ctx);

        TripBookingReviewMeResponseDto response = new TripBookingReviewMeResponseDto();
        tripBookingReviewRepository.findByBooking_IdAndCreatedUser_Id(bookingId, user.getId())
                .ifPresentOrElse(
                        review -> {
                            response.setHasReview(true);
                            response.setReview(toItemDtoWithEditWindow(review));
                        },
                        () -> response.setHasReview(false)
                );
        return response;
    }

    @Transactional
    public TripBookingReviewItemDto createReview(String username, String bookingId, TripBookingReviewCreateRequestDto request) {
        PoputkaUser reviewer = requireUser(username);
        BookingTripContext ctx = loadBookingContext(bookingId);
        assertParticipantOrNotFound(reviewer, ctx);

        if (ctx.booking().getBookingStatus() != BookingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review allowed only when booking is COMPLETED");
        }

        if (tripBookingReviewRepository.findByBooking_IdAndCreatedUser_Id(bookingId, reviewer.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review already exists for this booking");
        }

        PoputkaUser reviewee = resolveReviewee(reviewer, ctx);
        if (Objects.equals(reviewer.getId(), reviewee.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot review yourself");
        }

        TripBookingReview review = new TripBookingReview();
        review.setBooking(ctx.booking());
        review.setReviewee(reviewee);
        review.setStatus(TripBookingReviewStatus.DRAFT);
        review.setRating(request.getRating());
        review.setComment(normalizeComment(request.getComment()));

        TripBookingReview saved = tripBookingReviewRepository.save(review);
        return toItemDtoWithEditWindow(saved);
    }

    @Transactional
    public TripBookingReviewItemDto patchReview(String username, long reviewId, TripBookingReviewPatchRequestDto patch) {
        if (patch.getRating() == null && patch.getComment() == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "At least one of rating, comment is required");
        }

        PoputkaUser user = requireUser(username);
        TripBookingReview review = tripBookingReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        if (review.getCreatedUser() == null || review.getCreatedUser().getId() != user.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the author of this review");
        }
        if (review.getStatus() != TripBookingReviewStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review is not editable in status " + review.getStatus());
        }
        if (!isWithinEditWindow(review)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Edit window has expired");
        }

        if (patch.getRating() != null) {
            review.setRating(patch.getRating());
        }
        if (patch.getComment() != null) {
            review.setComment(normalizeComment(patch.getComment()));
        }

        TripBookingReview saved = tripBookingReviewRepository.save(review);
        return toItemDtoWithEditWindow(saved);
    }

    public Page<TripBookingReviewPublicListItemDto> listApprovedForUser(String actorUsername, long userId, Pageable pageable) {
        requireUser(actorUsername);
        PoputkaUser reviewee = poputkaUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Page<TripBookingReview> page = tripBookingReviewRepository.findByRevieweeAndStatusOrderByApprovedDatetimeDesc(
                reviewee,
                TripBookingReviewStatus.APPROVED,
                pageable
        );

        List<Long> tripIds = page.getContent().stream()
                .map(r -> r.getBooking().getTripId())
                .distinct()
                .toList();
        Map<Long, TripEntity> tripsById = tripRepository.findAllById(tripIds).stream()
                .collect(Collectors.toMap(TripEntity::getId, t -> t));

        List<TripBookingReviewPublicListItemDto> mapped = page.getContent().stream()
                .map(r -> toPublicListItem(r, tripsById))
                .toList();
        return new PageImpl<>(mapped, pageable, page.getTotalElements());
    }

    public Page<TripBookingReviewModerationListItemDto> listPendingModeration(String moderatorUsername, Pageable pageable) {
        assertModerator(moderatorUsername);
        return tripBookingReviewRepository
                .findByStatusOrderByCreatedDatetimeAsc(TripBookingReviewStatus.PENDING_MODERATION, pageable)
                .map(tripBookingReviewMapper::toModerationListItem);
    }

    @Transactional
    public TripBookingReviewModerationListItemDto moderate(String moderatorUsername, long reviewId, TripBookingReviewModerationRequestDto request) {
        assertModerator(moderatorUsername);
        TripBookingReview review = tripBookingReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        if (review.getStatus() != TripBookingReviewStatus.PENDING_MODERATION) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review is not pending moderation");
        }

        if (request.getDecision() == TripBookingReviewModerationDecision.APPROVE) {
            review.setStatus(TripBookingReviewStatus.APPROVED);
            review.setApprovedDatetime(Instant.now().toEpochMilli());
            review.setModeratorComment(request.getModeratorComment());
        } else {
            review.setStatus(TripBookingReviewStatus.REJECTED);
            review.setModeratorComment(request.getModeratorComment());
        }

        TripBookingReview saved = tripBookingReviewRepository.save(review);
        if (saved.getStatus() == TripBookingReviewStatus.APPROVED) {
            tripTripRatingService.recalculateTripRating(saved.getReviewee().getId());
        }
        return tripBookingReviewMapper.toModerationListItem(saved);
    }

    /**
     * Moves {@link TripBookingReviewStatus#DRAFT} reviews whose edit window has ended to {@link TripBookingReviewStatus#PENDING_MODERATION}.
     */
    @Transactional
    public int processDraftReviewsReadyForModeration() {
        long deadlineMillis = Instant.now().toEpochMilli() - properties.getDraftToModerationDelay().toMillis();
        List<TripBookingReview> drafts = tripBookingReviewRepository.findByStatusAndCreatedDatetimeLessThanEqual(
                TripBookingReviewStatus.DRAFT,
                deadlineMillis
        );
        for (TripBookingReview draft : drafts) {
            draft.setStatus(TripBookingReviewStatus.PENDING_MODERATION);
        }
        tripBookingReviewRepository.saveAll(drafts);
        return drafts.size();
    }

    private TripBookingReviewPublicListItemDto toPublicListItem(TripBookingReview review, Map<Long, TripEntity> tripsById) {
        TripEntity trip = tripsById.get(review.getBooking().getTripId());
        if (trip == null) {
            throw new IllegalStateException("Trip not found for booking " + review.getBooking().getId());
        }
        return new TripBookingReviewPublicListItemDto(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getApprovedDatetime(),
                trip.getPlaceFrom(),
                trip.getPlaceTo()
        );
    }

    private PoputkaUser resolveReviewee(PoputkaUser reviewer, BookingTripContext ctx) {
        if (Objects.equals(ctx.booking().getPassengerId(), reviewer.getId())) {
            return poputkaUserRepository.findById(ctx.trip().getOwnerId())
                    .orElseThrow(() -> new IllegalStateException("Trip owner user missing"));
        }
        return poputkaUserRepository.findById(ctx.booking().getPassengerId())
                .orElseThrow(() -> new IllegalStateException("Passenger user missing"));
    }

    private TripBookingReviewItemDto toItemDtoWithEditWindow(TripBookingReview review) {
        TripBookingReviewItemDto dto = tripBookingReviewMapper.toItemDto(review);
        long editableUntil = review.getCreatedDatetime() + properties.getDraftToModerationDelay().toMillis();
        dto.setEditableUntil(editableUntil);
        boolean canEdit = review.getStatus() == TripBookingReviewStatus.DRAFT && isWithinEditWindow(review);
        dto.setCanEdit(canEdit);
        return dto;
    }

    private boolean isWithinEditWindow(TripBookingReview review) {
        long deadline = review.getCreatedDatetime() + properties.getDraftToModerationDelay().toMillis();
        return Instant.now().toEpochMilli() <= deadline;
    }

    private void assertModerator(String username) {
        if (!properties.getModeratorUsernames().contains(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Moderation is not allowed for this user");
        }
    }

    private PoputkaUser requireUser(String username) {
        return poputkaUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
    }

    private BookingTripContext loadBookingContext(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        TripEntity trip = tripRepository.findById(booking.getTripId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        return new BookingTripContext(booking, trip);
    }

    private void assertParticipantOrNotFound(PoputkaUser user, BookingTripContext ctx) {
        boolean passenger = Objects.equals(ctx.booking().getPassengerId(), user.getId());
        boolean owner = Objects.equals(ctx.trip().getOwnerId(), user.getId());
        if (!passenger && !owner) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
        }
    }

    private static String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }
        String trimmed = comment.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record BookingTripContext(Booking booking, TripEntity trip) {
    }
}

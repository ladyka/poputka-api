package by.ladyka.poputka.controllers;

import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewCreateRequestDto;
import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewItemDto;
import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewMeResponseDto;
import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewModerationListItemDto;
import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewModerationRequestDto;
import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewPatchRequestDto;
import by.ladyka.poputka.data.dto.tripbookingreview.TripBookingReviewPublicListItemDto;
import by.ladyka.poputka.service.TripBookingReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/trip-booking-review")
@RequiredArgsConstructor
public class TripBookingReviewController {

    private final TripBookingReviewService tripBookingReviewService;

    @GetMapping("/booking/{bookingId}/me")
    public TripBookingReviewMeResponseDto myReview(Principal principal, @PathVariable String bookingId) {
        return tripBookingReviewService.getMyReview(principal.getName(), bookingId);
    }

    @PostMapping("/booking/{bookingId}")
    public TripBookingReviewItemDto createReview(
            Principal principal,
            @PathVariable String bookingId,
            @RequestBody @Valid TripBookingReviewCreateRequestDto request
    ) {
        return tripBookingReviewService.createReview(principal.getName(), bookingId, request);
    }

    @PatchMapping("/{reviewId}")
    public TripBookingReviewItemDto patchReview(
            Principal principal,
            @PathVariable long reviewId,
            @RequestBody @Valid TripBookingReviewPatchRequestDto request
    ) {
        return tripBookingReviewService.patchReview(principal.getName(), reviewId, request);
    }

    @GetMapping("/users/{userId}")
    public Page<TripBookingReviewPublicListItemDto> listApprovedForUser(
            Principal principal,
            @PathVariable long userId,
            Pageable pageable
    ) {
        return tripBookingReviewService.listApprovedForUser(principal.getName(), userId, pageable);
    }

    @GetMapping("/moderation/pending")
    public Page<TripBookingReviewModerationListItemDto> pendingModeration(Principal principal, Pageable pageable) {
        return tripBookingReviewService.listPendingModeration(principal.getName(), pageable);
    }

    @PutMapping("/moderation/{reviewId}")
    public TripBookingReviewModerationListItemDto moderate(
            Principal principal,
            @PathVariable long reviewId,
            @RequestBody @Valid TripBookingReviewModerationRequestDto request
    ) {
        return tripBookingReviewService.moderate(principal.getName(), reviewId, request);
    }
}

package by.ladyka.poputka.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class TripBookingReviewDraftScheduler {

    private final TripBookingReviewService tripBookingReviewService;

    @Scheduled(fixedDelayString = "${poputka.trip-booking-review.poll-ms:60000}")
    public void moveExpiredDraftsToModeration() {
        tripBookingReviewService.processDraftReviewsReadyForModeration();
    }
}

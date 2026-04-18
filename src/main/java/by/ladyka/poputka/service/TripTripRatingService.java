package by.ladyka.poputka.service;

import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripBookingReview;
import by.ladyka.poputka.data.enums.TripBookingReviewStatus;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import by.ladyka.poputka.data.repository.TripBookingReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripTripRatingService {

    private static final int RECENT_APPROVED_LIMIT = 50;

    private final TripBookingReviewRepository tripBookingReviewRepository;
    private final PoputkaUserRepository poputkaUserRepository;

    @Transactional
    public void recalculateTripRating(long revieweeUserId) {
        PoputkaUser reviewee = poputkaUserRepository.findById(revieweeUserId).orElseThrow();
        List<TripBookingReview> recent = tripBookingReviewRepository
                .findTop50ByRevieweeAndStatusOrderByApprovedDatetimeDesc(reviewee, TripBookingReviewStatus.APPROVED);

        if (recent.isEmpty()) {
            reviewee.setTripRating(BigDecimal.ZERO);
            return;
        }

        BigDecimal sum = recent.stream()
                .map(TripBookingReview::getRating)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = sum.divide(BigDecimal.valueOf(recent.size()), 2, RoundingMode.HALF_UP);
        reviewee.setTripRating(average);
    }
}

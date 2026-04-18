package by.ladyka.poputka.data.repository;

import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripBookingReview;
import by.ladyka.poputka.data.enums.TripBookingReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripBookingReviewRepository extends JpaRepository<TripBookingReview, Long> {

    Optional<TripBookingReview> findByBooking_IdAndCreatedUser_Id(String bookingId, Long reviewerId);

    Page<TripBookingReview> findByRevieweeAndStatusOrderByApprovedDatetimeDesc(
            PoputkaUser reviewee,
            TripBookingReviewStatus status,
            Pageable pageable
    );

    List<TripBookingReview> findByStatusAndCreatedDatetimeLessThanEqual(
            TripBookingReviewStatus status,
            long createdDatetimeDeadlineInclusive
    );

    Page<TripBookingReview> findByStatusOrderByCreatedDatetimeAsc(
            TripBookingReviewStatus status,
            Pageable pageable
    );

    List<TripBookingReview> findTop50ByRevieweeAndStatusOrderByApprovedDatetimeDesc(
            PoputkaUser reviewee,
            TripBookingReviewStatus status
    );
}

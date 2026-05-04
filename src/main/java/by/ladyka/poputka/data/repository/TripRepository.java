package by.ladyka.poputka.data.repository;

import by.ladyka.poputka.data.entity.TripEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, Long> {
    Optional<TripEntity> findByIdAndOwnerId(Long id, long owner);

    Page<TripEntity> findAllByOwnerId(long ownerId, Pageable pageable);

    Page<TripEntity> findAllByOwnerIdAndStartGreaterThanEqual(long ownerId, long startMinInclusiveEpochMillis, Pageable pageable);

    Page<TripEntity> findAllByOwnerIdAndStartLessThan(long ownerId, long startMaxExclusiveEpochMillis, Pageable pageable);

    @Query("SELECT t FROM TripEntity t WHERE t.id IN (SELECT b.tripId FROM Booking b WHERE b.passengerId = :passengerId)")
    Page<TripEntity> findAllTripsWithPassengerBooking(@Param("passengerId") long passengerId, Pageable pageable);

    @Query("SELECT t FROM TripEntity t WHERE t.id IN (SELECT b.tripId FROM Booking b WHERE b.passengerId = :passengerId) AND t.start >= :startMinInclusiveEpochMillis")
    Page<TripEntity> findAllTripsWithPassengerBookingAndStartGreaterThanEqual(
            @Param("passengerId") long passengerId,
            @Param("startMinInclusiveEpochMillis") long startMinInclusiveEpochMillis,
            Pageable pageable);

    @Query("SELECT t FROM TripEntity t WHERE t.id IN (SELECT b.tripId FROM Booking b WHERE b.passengerId = :passengerId) AND t.start < :startMaxExclusiveEpochMillis")
    Page<TripEntity> findAllTripsWithPassengerBookingAndStartLessThan(
            @Param("passengerId") long passengerId,
            @Param("startMaxExclusiveEpochMillis") long startMaxExclusiveEpochMillis,
            Pageable pageable);

    @Query("SELECT DISTINCT t FROM TripEntity t WHERE t.ownerId = :userId OR t.id IN (SELECT b.tripId FROM Booking b WHERE b.passengerId = :userId)")
    Page<TripEntity> findAllTripsWhereOwnerOrPassengerBooking(@Param("userId") long userId, Pageable pageable);

    @Query("SELECT DISTINCT t FROM TripEntity t WHERE (t.ownerId = :userId OR t.id IN (SELECT b.tripId FROM Booking b WHERE b.passengerId = :userId)) AND t.start >= :startMinInclusiveEpochMillis")
    Page<TripEntity> findAllTripsWhereOwnerOrPassengerBookingAndStartGreaterThanEqual(
            @Param("userId") long userId,
            @Param("startMinInclusiveEpochMillis") long startMinInclusiveEpochMillis,
            Pageable pageable);

    @Query("SELECT DISTINCT t FROM TripEntity t WHERE (t.ownerId = :userId OR t.id IN (SELECT b.tripId FROM Booking b WHERE b.passengerId = :userId)) AND t.start < :startMaxExclusiveEpochMillis")
    Page<TripEntity> findAllTripsWhereOwnerOrPassengerBookingAndStartLessThan(
            @Param("userId") long userId,
            @Param("startMaxExclusiveEpochMillis") long startMaxExclusiveEpochMillis,
            Pageable pageable);

    Page<TripEntity> findAllByPlaceFromAndPlaceToAndStartIsGreaterThan(String placeFrom, String placeTo, Long start, Pageable pageable);

    @Query(value = "select  t.place_from, t.place_to, count(*) as c from trips t where t.start > (SELECT extract(epoch from now() at time zone 'utc'))*1000 group by t.place_from, t.place_to  order by c;", nativeQuery = true)
    List<Object[]> findTop10Routes();
}

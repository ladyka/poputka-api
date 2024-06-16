package by.ladyka.poputka.data.repository;

import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, Long> {
    Optional<TripEntity> findByIdAndOwner(Long id, PoputkaUser owner);

    Page<TripEntity> findAllByPlaceFromAndPlaceTo(String placeFrom, String placeTo, Pageable pageable);
}

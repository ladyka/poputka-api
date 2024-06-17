package by.ladyka.poputka.data.repository;

import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, Long> {
    Optional<TripEntity> findByIdAndOwner(Long id, PoputkaUser owner);

    Page<TripEntity> findAllByPlaceFromAndPlaceToAndStartIsGreaterThan(String placeFrom, String placeTo, Long start, Pageable pageable);

    @Query(value = "select  t.place_from, t.place_to, count(*) as c from trips t where t.start > (SELECT extract(epoch from now() at time zone 'utc')) group by t.place_from, t.place_to  order by c LIMIT 10;", nativeQuery = true)
    List<Object[]> findTop10Places();
}

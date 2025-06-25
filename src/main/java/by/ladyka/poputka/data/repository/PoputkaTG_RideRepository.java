package by.ladyka.poputka.data.repository;

import by.ladyka.poputka.data.entity.PoputkaTG_Ride;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoputkaTG_RideRepository extends JpaRepository<PoputkaTG_Ride, Integer> {

    @Query(value = "select  t.from_place_now, t.to_place_now, count(*) as c from poputka_tg_rides t where t.start_ride > (select current_date) group by t.from_place_now, t.to_place_now  order by c desc LIMIT 128;", nativeQuery = true)
    List<Object[]> findTop10Routes();

    Page<PoputkaTG_Ride> findAllByFromPlaceNowAndToPlaceNow(String fromPlaceNow, String toPlaceNow, Pageable page);
}

package by.ladyka.poputka.data.repository;

import by.ladyka.poputka.data.entity.PoputkaTG_Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoputkaTG_RideRepository extends JpaRepository<PoputkaTG_Ride, Integer> {
}

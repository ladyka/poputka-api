package by.ladyka.poputka.data.repository.roadassistance;

import by.ladyka.poputka.data.entity.roadassistance.Competency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetencyRepository extends JpaRepository<Competency, Long> {
    List<Competency> findByRelatedTypeId(Long relatedTypeId);
}

package by.ladyka.poputka.data.repository.roadassistance;

import by.ladyka.poputka.data.entity.roadassistance.AssistanceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssistanceTypeRepository extends JpaRepository<AssistanceType, Long> {
    Optional<AssistanceType> findByCode(String code);
}

package by.ladyka.poputka.data.repository.roadassistance;

import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceRequest;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface AssistanceRequestRepository extends JpaRepository<AssistanceRequest, Long> {
    Page<AssistanceRequest> findByStatusIn(List<AssistanceRequestStatus> statuses, Pageable pageable);
    
    List<AssistanceRequest> findByCreatedUserAndStatusIn(PoputkaUser createdUser, Collection<AssistanceRequestStatus> status);
    
    List<AssistanceRequest> findByStatusAndExpiresAtBefore(AssistanceRequestStatus status, Instant expiresAt);
    
    @Query("SELECT r FROM AssistanceRequest r WHERE r.status IN :statuses " +
           "AND r.locationLat BETWEEN :minLat AND :maxLat " +
           "AND r.locationLon BETWEEN :minLon AND :maxLon")
    Page<AssistanceRequest> findByStatusInAndLocationNear(
            @Param("statuses") List<AssistanceRequestStatus> statuses,
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLon") BigDecimal minLon,
            @Param("maxLon") BigDecimal maxLon,
            Pageable pageable);
}

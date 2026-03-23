package by.ladyka.poputka.data.repository.roadassistance;

import by.ladyka.poputka.data.entity.roadassistance.AssistanceOffer;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceOfferStatus;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceRequest;
import by.ladyka.poputka.data.entity.PoputkaUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssistanceOfferRepository extends JpaRepository<AssistanceOffer, Long> {
    Optional<AssistanceOffer> findByRequestIdAndCreatedUser(Long request_id, PoputkaUser createdUser);
    
    List<AssistanceOffer> findByRequestIdAndStatus(Long requestId, AssistanceOfferStatus status);
}

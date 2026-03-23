package by.ladyka.poputka.controllers.roadassistance;

import by.ladyka.poputka.ApplicationUserDetails;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceOffer;
import by.ladyka.poputka.service.roadassistance.AssistanceOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roadassistance")
@RequiredArgsConstructor
public class AssistanceOfferController {
    private final AssistanceOfferService assistanceOfferService;

    @PostMapping("/requests/{requestId}/offers")
    public ResponseEntity<AssistanceOffer> createOffer(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @PathVariable Long requestId,
            @RequestParam(required = false) String message) {
        
        AssistanceOffer offer = assistanceOfferService.createOffer(user.user(), requestId, message);
        return ResponseEntity.ok(offer);
    }

    @PostMapping("/offers/{offerId}/cancel")
    public ResponseEntity<AssistanceOffer> cancelOffer(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @PathVariable Long offerId) {
        
        AssistanceOffer offer = assistanceOfferService.cancelOffer(user.user(), offerId);
        return ResponseEntity.ok(offer);
    }

    @GetMapping("/requests/{requestId}/offers/my")
    public ResponseEntity<AssistanceOffer> getMyOffer(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @PathVariable Long requestId) {
        
        AssistanceOffer offer = assistanceOfferService.getUserOfferForRequest(user.user(), requestId);
        if (offer != null) {
            return ResponseEntity.ok(offer);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/requests/{requestId}/offers/{offerId}/accept")
    public ResponseEntity<AssistanceOffer> acceptOffer(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @PathVariable Long requestId,
            @PathVariable Long offerId) {
        
        AssistanceOffer offer = assistanceOfferService.acceptOffer(user.user(), requestId, offerId);
        return ResponseEntity.ok(offer);
    }

    @PostMapping("/requests/{requestId}/offers/{offerId}/reject")
    public ResponseEntity<AssistanceOffer> rejectOffer(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @PathVariable Long requestId,
            @PathVariable Long offerId) {
        
        AssistanceOffer offer = assistanceOfferService.rejectOffer(user.user(), requestId, offerId);
        return ResponseEntity.ok(offer);
    }
}

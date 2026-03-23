package by.ladyka.poputka.controllers.roadassistance;

import by.ladyka.poputka.ApplicationUserDetails;
import by.ladyka.poputka.data.dto.roadassistance.AssistanceRequestDto;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceRequest;
import by.ladyka.poputka.service.roadassistance.AssistanceRequestService;
import by.ladyka.poputka.service.roadassistance.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/roadassistance/requests")
@RequiredArgsConstructor
    public class AssistanceRequestController {
    private final AssistanceRequestService assistanceRequestService;
    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<Page<AssistanceRequestDto>> searchRequests(
            @RequestParam(required = false) BigDecimal lat,
            @RequestParam(required = false) BigDecimal lon,
            @RequestParam(defaultValue = "10") BigDecimal radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AssistanceRequestDto> requests = assistanceRequestService
            .findActiveRequestsNearby(lat, lon, radiusKm, pageable);
        
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{assistanceRequestId}")
    public ResponseEntity<AssistanceRequestDto> getRequest(@PathVariable Long assistanceRequestId) {
        AssistanceRequestDto request = assistanceRequestService.getRequest(assistanceRequestId);
        if (request != null) {
            return ResponseEntity.ok(request);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<AssistanceRequestDto> createRequest(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @RequestParam String carInfo,
            @RequestParam String problemTypeCode,
            @RequestParam String description,
            @RequestParam BigDecimal locationLat,
            @RequestParam BigDecimal locationLon,
            @RequestParam(required = false) String address) {
        
        AssistanceRequestDto request = assistanceRequestService.createRequest(
            user.user(), carInfo, problemTypeCode, description, locationLat, locationLon, address);
        
        return ResponseEntity.ok(request);
    }

    @PostMapping("/{id}/extend")
    public ResponseEntity<AssistanceRequest> extendRequest(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @PathVariable Long id,
            @RequestParam int hours) {
        
        AssistanceRequest request = assistanceRequestService.extendRequest(user.user(), id, hours);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<AssistanceRequest> cancelRequest(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @PathVariable Long id) {
        
        AssistanceRequest request = assistanceRequestService.cancelRequest(user.user(), id);
        return ResponseEntity.ok(request);
    }

    @GetMapping("/active/count")
    public ResponseEntity<Long> getActiveRequestsCount(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lon,
            @RequestParam(defaultValue = "10") BigDecimal radiusKm) {
        
        long count = assistanceRequestService.countActiveRequestsNearby(lat, lon, radiusKm);
        return ResponseEntity.ok(count);
    }
}

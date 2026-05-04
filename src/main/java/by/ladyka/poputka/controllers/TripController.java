package by.ladyka.poputka.controllers;

import by.ladyka.poputka.data.dto.PopularRouteDto;
import by.ladyka.poputka.data.dto.TripDto;
import by.ladyka.poputka.data.dto.TripCreateRequestDto;
import by.ladyka.poputka.data.dto.TripUpdateRequestDto;
import by.ladyka.poputka.data.dto.TripSearchRequest;
import by.ladyka.poputka.service.TripService;
import by.ladyka.poputka.ApplicationUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(TripController.API_TRIP)
@RequiredArgsConstructor
public class TripController {
    public static final String API_TRIP = "/api/trip";
    private final TripService tripService;

    @PostMapping("/")
    public TripDto create(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @RequestBody TripCreateRequestDto dto
    ) {
        return tripService.create(user, dto);
    }

    @PutMapping("/{id}")
    public TripDto update(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @PathVariable("id") Long id,
            @RequestBody TripUpdateRequestDto dto
    ) {
        return tripService.update(user, id, dto);
    }

    @PostMapping("/search")
    public List<TripDto> findTrips(@RequestBody TripSearchRequest tripSearchRequest) {
        return tripService.search(tripSearchRequest);
    }

    @GetMapping("/owned")
    public Page<TripDto> ownedTrips(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @RequestParam(name = "timeFilter", required = false, defaultValue = "all") String timeFilter,
            @RequestParam(name = "participant", required = false) String participant,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return tripService.findOwnedTrips(user, timeFilter, participant, pageable);
    }

    @GetMapping("/{id}")
    public TripDto findById(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @PathVariable("id") Long id
    ) {
        return tripService.findById(user, id);
    }

    @GetMapping("/popular")
    public List<PopularRouteDto> popular() {
        return tripService.popular();
    }
}

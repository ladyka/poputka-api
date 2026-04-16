package by.ladyka.poputka.controllers;

import by.ladyka.poputka.data.dto.PopularRouteDto;
import by.ladyka.poputka.data.dto.TripDto;
import by.ladyka.poputka.data.dto.TripRequestDto;
import by.ladyka.poputka.data.dto.TripSearchRequest;
import by.ladyka.poputka.data.enums.BookingStatus;
import by.ladyka.poputka.data.entity.TripEntity;
import by.ladyka.poputka.data.repository.BookingRepository;
import by.ladyka.poputka.data.repository.TripRepository;
import by.ladyka.poputka.service.mapper.TripMapper;
import by.ladyka.poputka.ApplicationUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(TripController.API_TRIP)
@RequiredArgsConstructor
public class TripController {
    public static final String API_TRIP = "/api/trip";
    private static final long RECENT_TRIP_VISIBILITY_DAYS = 7;
    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;
    private final TripMapper tripMapper;

    @PostMapping("/")
    public TripDto update(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @RequestBody TripRequestDto dto
    ) {
        TripEntity trip;
        if (dto.getId() == null || dto.getId() == -1L) {
            trip = new TripEntity();
            trip.setOwnerId(user.user().getId());
        } else {
            trip = tripRepository.findByIdAndOwnerId(dto.getId(), user.user().getId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        }
        tripMapper.toEntity(dto, trip);
        TripEntity entity = tripRepository.save(trip);
        return tripMapper.toDto(entity);
    }

    @PostMapping("/search")
    public List<TripDto> findTrips(@RequestBody TripSearchRequest tripSearchRequest) {
        return tripRepository.findAllByPlaceFromAndPlaceToAndStartIsGreaterThan(tripSearchRequest.getPlaceFrom(),
                        tripSearchRequest.getPlaceTo(), Instant.now().toEpochMilli(),
                        Pageable.unpaged(
                                Sort.by("start")))
                .map(tripMapper::toDto)
                .stream().toList();
    }

    @GetMapping("/{id}")
    public TripDto findById(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @PathVariable("id") Long id
    ) {
        TripEntity entity = tripRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        if (Instant.now().minus(RECENT_TRIP_VISIBILITY_DAYS, ChronoUnit.DAYS).isAfter(entity.getStartTime())) {
            if (user == null || user.user() == null) {
                throw new ResponseStatusException(NOT_FOUND);
            }
            long userId = user.user().getId();
            boolean allowed = (entity.getOwnerId() == userId)
                              || bookingRepository.findBookingByTripIdAndPassengerId(entity.getId(), userId)
                                      .filter(b -> BookingStatus.ACCEPTED.equals(b.getBookingStatus()))
                                      .isPresent();
            if (!allowed) {
                throw new ResponseStatusException(NOT_FOUND);
            }
        }
        return tripMapper.toDto(entity);
    }

    @GetMapping("/popular")
    public List<PopularRouteDto> popular() {

        List<Object[]> trips = tripRepository.findTop10Routes();

        return trips.stream()
                .map(objects -> {
                    PopularRouteDto popularRouteDto = new PopularRouteDto();
                    popularRouteDto.setPlaceFrom(String.valueOf(objects[0]));
                    popularRouteDto.setPlaceTo(String.valueOf(objects[1]));
                    popularRouteDto.setC(Integer.parseInt(String.valueOf(objects[2])));
                    return popularRouteDto;
                })
                .sorted(Comparator.comparingInt(PopularRouteDto::getC).reversed())
                .toList();

    }

}

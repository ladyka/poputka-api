package by.ladyka.poputka.controllers;

import by.ladyka.poputka.data.dto.PopularRouteDto;
import by.ladyka.poputka.data.dto.TripDto;
import by.ladyka.poputka.data.dto.TripRequestDto;
import by.ladyka.poputka.data.dto.TripSearchRequest;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripEntity;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import by.ladyka.poputka.data.repository.TripRepository;
import by.ladyka.poputka.service.mapper.TripMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
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
    private final TripRepository tripRepository;
    private final PoputkaUserRepository poputkaUserRepository;
    private final TripMapper tripMapper;

    @PostMapping("/")
    public TripDto update(Principal principal, @RequestBody TripRequestDto dto) {
        PoputkaUser tripOwner = poputkaUserRepository.findByUsername(principal.getName()).orElseThrow();
        TripEntity trip;
        if (-1 == dto.getId()) {
            trip = new TripEntity();
            trip.setOwnerId(tripOwner.getId());
        } else {
            trip = tripRepository.findByIdAndOwnerId(dto.getId(), tripOwner.getId()).orElseThrow();
        }
        tripMapper.toEntity(dto, trip);
        TripEntity entity = tripRepository.save(trip);
        return tripMapper.toDto(entity);
    }

    @PostMapping("/search")
    public List<TripDto> findTrips(@RequestBody TripSearchRequest tripSearchRequest) {
        List<TripDto> web = tripRepository.findAllByPlaceFromAndPlaceToAndStartIsGreaterThan(tripSearchRequest.getPlaceFrom(),
                        tripSearchRequest.getPlaceTo(), System.currentTimeMillis() / 1000,
                        Pageable.unpaged(
                                Sort.by("start")))
                .map(tripMapper::toDto)
                .stream().toList();
        List<TripDto> internal = new ArrayList<>();
        internal.addAll(web);
        return internal;
    }

    @GetMapping("/{id}")
    public TripDto findById(@PathVariable("id") Long id) {
            return tripRepository
                    .findById(id)
                    .filter(tripEntity -> Instant.now().minus(7, ChronoUnit.DAYS).isBefore(tripEntity.getStartTime()))
                    .map(tripMapper::toDto)
                    .orElseThrow();
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

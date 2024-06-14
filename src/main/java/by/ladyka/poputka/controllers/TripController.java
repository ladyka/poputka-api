package by.ladyka.poputka.controllers;

import by.ladyka.poputka.TripMapper;
import by.ladyka.poputka.data.dto.TripDto;
import by.ladyka.poputka.data.dto.TripRequestDto;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripEntity;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import by.ladyka.poputka.data.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
public class TripController {
    private final TripRepository tripRepository;
    private final PoputkaUserRepository poputkaUserRepository;
    private final TripMapper tripMapper;

    @PostMapping("/")
    public TripDto update(Principal principal, @RequestBody TripRequestDto dto) {
        log.debug("Update trip: {}", dto);
        PoputkaUser tripOwner = poputkaUserRepository.findByUsername(principal.getName()).orElseThrow();
        TripEntity trip;
        if (-1 == dto.getId()) {
            trip = new TripEntity();
            trip.setOwner(tripOwner);
        } else {
            trip = tripRepository.findByIdAndOwner(dto.getId(), tripOwner).orElseThrow();
        }
        tripMapper.toEntity(dto, trip);
        TripEntity entity = tripRepository.save(trip);
        return tripMapper.toDto(entity);
    }

    @GetMapping("/{id}")
    public TripDto findById(@PathVariable("id") Long id) {
        return tripRepository
                .findById(id)
                .filter(tripEntity -> Instant.now().minus(7, ChronoUnit.DAYS).isBefore(tripEntity.getStartTime()))
                .map(tripMapper::toDto)
                .orElseThrow();
    }
}

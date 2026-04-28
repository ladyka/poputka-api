package by.ladyka.poputka.service;

import by.ladyka.poputka.ApplicationUserDetails;
import by.ladyka.poputka.data.dto.PopularRouteDto;
import by.ladyka.poputka.data.dto.TripCreateRequestDto;
import by.ladyka.poputka.data.dto.TripDto;
import by.ladyka.poputka.data.dto.TripSearchRequest;
import by.ladyka.poputka.data.dto.TripUpdateRequestDto;
import by.ladyka.poputka.data.enums.BookingStatus;
import by.ladyka.poputka.data.enums.OwnedTripTimeFilter;
import by.ladyka.poputka.data.entity.TripEntity;
import by.ladyka.poputka.data.repository.BookingRepository;
import by.ladyka.poputka.data.repository.TripRepository;
import by.ladyka.poputka.service.mapper.TripMapper;
import by.ladyka.poputka.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TripService {
    private static final long RECENT_TRIP_VISIBILITY_DAYS = 7;

    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;
    private final TripMapper tripMapper;
    private final NotificationService notificationService;

    public TripDto create(ApplicationUserDetails user, TripCreateRequestDto dto) {
        validateTrip(dto.getStartEpochMillis(), dto.getPassengers());

        TripEntity trip = new TripEntity();
        trip.setOwnerId(user.user().getId());
        tripMapper.toEntity(dto, trip);
        TripEntity entity = tripRepository.save(trip);
        notificationService.sendAdminTripCreatedNotification(entity);
        return tripMapper.toDto(entity);
    }

    public TripDto update(ApplicationUserDetails user, long id, TripUpdateRequestDto dto) {
        validateTrip(dto.getStartEpochMillis(), dto.getPassengers());

        TripEntity trip = tripRepository.findByIdAndOwnerId(id, user.user().getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        tripMapper.toEntity(dto, trip);
        TripEntity entity = tripRepository.save(trip);
        return tripMapper.toDto(entity);
    }

    public Page<TripDto> findOwnedTrips(ApplicationUserDetails user, String timeFilterRaw, Pageable pageable) {
        long ownerId = user.user().getId();
        OwnedTripTimeFilter filter = parseOwnedTripTimeFilter(timeFilterRaw);
        Pageable effective = pageable.getSort().isSorted()
                               ? pageable
                               : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "start"));
        long now = Instant.now().toEpochMilli();
        Page<TripEntity> page = switch (filter) {
            case ALL -> tripRepository.findAllByOwnerId(ownerId, effective);
            case UPCOMING -> tripRepository.findAllByOwnerIdAndStartGreaterThanEqual(ownerId, now, effective);
            case PAST -> tripRepository.findAllByOwnerIdAndStartLessThan(ownerId, now, effective);
        };
        return page.map(tripMapper::toDto);
    }

    public List<TripDto> search(TripSearchRequest tripSearchRequest) {
        return tripRepository.findAllByPlaceFromAndPlaceToAndStartIsGreaterThan(
                        tripSearchRequest.getPlaceFrom(),
                        tripSearchRequest.getPlaceTo(),
                        Instant.now().toEpochMilli(),
                        org.springframework.data.domain.Pageable.unpaged(
                                org.springframework.data.domain.Sort.by("start")))
                .map(tripMapper::toDto)
                .stream().toList();
    }

    public TripDto findById(ApplicationUserDetails user, long id) {
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

    private OwnedTripTimeFilter parseOwnedTripTimeFilter(String raw) {
        if (raw == null || raw.isBlank()) {
            return OwnedTripTimeFilter.ALL;
        }
        try {
            return OwnedTripTimeFilter.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "timeFilter must be all, upcoming, or past");
        }
    }

    private void validateTrip(Long startEpochMillis, int passengers) {
        if (startEpochMillis == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startEpochMillis is required");
        }
        Instant start = Instant.ofEpochMilli(startEpochMillis);
        if (start.isBefore(Instant.now().plus(1, ChronoUnit.HOURS))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start must be at least 1 hour in the future");
        }
        if (passengers < 0 || passengers > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "passengers must be between 0 and 100");
        }
    }
}


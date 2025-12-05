package by.ladyka.poputka.service.mapper;

import by.ladyka.poputka.data.dto.TripDto;
import by.ladyka.poputka.data.dto.TripRequestDto;
import by.ladyka.poputka.data.entity.PoputkaTG_Ride;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripEntity;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class TripMapper {
    private final PoputkaUserRepository poputkaUserRepository;

    public TripDto toDto(TripEntity entity) {
        TripDto dto = new TripDto();
        PoputkaUser owner = poputkaUserRepository.findById(entity.getOwnerId()).orElseThrow();
        dto.setId(entity.getId());
        dto.setFrom(entity.getPlaceFrom());
        dto.setTo(entity.getPlaceTo());
        dto.setStart(entity.getStartTime());
//        dto.setPrice(entity.getPrice());
//        dto.setCurrency(entity.getCurrency());
        dto.setCar(owner.getCar());
        dto.setDescription(entity.getDescription());
        dto.setPassengers(entity.getPassengers());
        dto.setOwner(owner.getName());
        dto.setOwnerTelegramUsername(owner.getTelegramUsername());
        return dto;
    }

    public TripDto toDto(PoputkaTG_Ride entity) {
        TripDto dto = new TripDto();
        dto.setId(entity.getRideId() * -1);
        dto.setFrom(entity.getFromPlaceNow());
        dto.setTo(entity.getToPlaceNow());
        ZonedDateTime start = ZonedDateTime.of(entity.getStartRide(), LocalTime.of(entity.getTimeStart(), 0), ZoneId.systemDefault());
        dto.setStart(start.toInstant());
        dto.setPrice(BigDecimal.ZERO);
        dto.setCurrency("");
        dto.setCar(entity.getCar());
        dto.setDescription(entity.getDriverWishes());
        dto.setPassengers((byte) 1);
        dto.setOwner(entity.getUserNickname());
        dto.setOwnerTelegramUsername(entity.getUserNickname());
        return dto;
    }

    public void toEntity(TripRequestDto dto, TripEntity entity) {
        entity.setPlaceFrom(dto.getFrom().getName());
        entity.setPlaceTo(dto.getTo().getName());
        entity.setStartTime(dto.getStart());
//        entity.setPrice(dto.getPrice());
//        entity.setCurrency(dto.getCurrency());
        entity.setDescription(dto.getDescription());
        entity.setPassengers(dto.getPassengers());
        entity.setId(dto.getId());
    }
}

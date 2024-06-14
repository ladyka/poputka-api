package by.ladyka.poputka;

import by.ladyka.poputka.data.dto.TripDto;
import by.ladyka.poputka.data.dto.TripRequestDto;
import by.ladyka.poputka.data.entity.TripEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TripMapper {
    public TripDto toDto(TripEntity entity) {
        TripDto dto = new TripDto();
        dto.setId(entity.getId());
        dto.setFrom(entity.getPlaceFrom());
        dto.setTo(entity.getPlaceTo());
        dto.setStart(entity.getStartTime());
        dto.setPrice(entity.getPrice());
        dto.setCurrency(entity.getCurrency());
        dto.setCar(entity.getCar());
        dto.setDescription(entity.getDescription());
        dto.setPassengers(entity.getPassengers());
        dto.setOwner(entity.getOwner().getName());
        return dto;
    }

    public TripEntity toEntity(TripRequestDto dto, TripEntity entity) {
        entity.setPlaceFrom(dto.getFrom());
        entity.setPlaceTo(dto.getTo());
        //        entity.setStart(dto.getStart());
        entity.setStartTime(Instant.now());
        entity.setPrice(dto.getPrice());
        entity.setCurrency(dto.getCurrency());
        entity.setCar(dto.getCar());
        entity.setDescription(dto.getDescription());
        entity.setPassengers(dto.getPassengers());
        entity.setId(dto.getId());
        return entity;
    }
}

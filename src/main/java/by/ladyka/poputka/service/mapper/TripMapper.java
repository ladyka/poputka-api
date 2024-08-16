package by.ladyka.poputka.service.mapper;

import by.ladyka.poputka.data.dto.TripDto;
import by.ladyka.poputka.data.dto.TripRequestDto;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripEntity;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        dto.setPrice(entity.getPrice());
        dto.setCurrency(entity.getCurrency());
        dto.setCar(owner.getCar());
        dto.setDescription(entity.getDescription());
        dto.setPassengers(entity.getPassengers());
        dto.setOwner(owner.getName());
        dto.setOwnerTelegramUsername(owner.getTelegramUsername());
        return dto;
    }

    public void toEntity(TripRequestDto dto, TripEntity entity) {
        entity.setPlaceFrom(dto.getFrom());
        entity.setPlaceTo(dto.getTo());
        entity.setStartTime(dto.getStart());
        entity.setPrice(dto.getPrice());
        entity.setCurrency(dto.getCurrency());
        entity.setDescription(dto.getDescription());
        entity.setPassengers(dto.getPassengers());
        entity.setId(dto.getId());
    }
}

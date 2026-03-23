package by.ladyka.poputka.service.mapper.roadassistance;

import by.ladyka.poputka.data.dto.roadassistance.AssistanceOfferDto;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceOffer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssistanceOfferMapper {
    @Mapping(target = "requestId", source = "entity.request.id")
    @Mapping(target = "helperId", source = "entity.helper.id")
    @Mapping(target = "helperName", source = "entity.helper.name")
    @Mapping(target = "status", source = "entity.status")
    AssistanceOfferDto toDto(AssistanceOffer entity);
    
    List<AssistanceOfferDto> toDtoList(List<AssistanceOffer> entities);
}

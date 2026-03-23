package by.ladyka.poputka.service.mapper.roadassistance;

import by.ladyka.poputka.data.dto.roadassistance.CompetencyDto;
import by.ladyka.poputka.data.entity.roadassistance.Competency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompetencyMapper {
    @Mapping(target = "relatedTypeId", source = "entity.relatedType.id")
    CompetencyDto toDto(Competency entity);
    
    List<CompetencyDto> toDtoList(List<Competency> entities);
}

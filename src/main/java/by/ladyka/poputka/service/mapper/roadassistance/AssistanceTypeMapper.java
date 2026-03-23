package by.ladyka.poputka.service.mapper.roadassistance;

import by.ladyka.poputka.data.dto.roadassistance.AssistanceTypeDto;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssistanceTypeMapper {
    AssistanceTypeDto toDto(AssistanceType entity);
    
    List<AssistanceTypeDto> toDtoList(List<AssistanceType> entities);
}

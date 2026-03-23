package by.ladyka.poputka.service.mapper.roadassistance;

import by.ladyka.poputka.data.dto.roadassistance.AssistanceRequestDto;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssistanceRequestMapper {
    @Mapping(target = "authorId", source = "entity.author.id")
    @Mapping(target = "authorName", source = "entity.author.name")
    @Mapping(target = "problemTypeId", source = "entity.problemType.id")
    @Mapping(target = "problemTypeName", source = "entity.problemType.name")
    @Mapping(target = "status", source = "entity.status")
    AssistanceRequestDto toDto(AssistanceRequest entity);
    
    List<AssistanceRequestDto> toDtoList(List<AssistanceRequest> entities);
}

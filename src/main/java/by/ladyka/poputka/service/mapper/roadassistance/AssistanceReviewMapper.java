package by.ladyka.poputka.service.mapper.roadassistance;

import by.ladyka.poputka.data.dto.roadassistance.AssistanceReviewDto;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssistanceReviewMapper {
    @Mapping(target = "requestId", source = "entity.request.id")
    @Mapping(target = "reviewerId", source = "entity.reviewer.id")
    @Mapping(target = "reviewerName", source = "entity.reviewer.name")
    @Mapping(target = "revieweeId", source = "entity.reviewee.id")
    @Mapping(target = "revieweeName", source = "entity.reviewee.name")
    AssistanceReviewDto toDto(AssistanceReview entity);
    
    List<AssistanceReviewDto> toDtoList(List<AssistanceReview> entities);
}

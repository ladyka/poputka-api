package by.ladyka.poputka.data.dto.roadassistance;

import lombok.Data;

import java.time.Instant;

@Data
public class CompetencyDto {
    private Long id;
    private String name;
    private Long relatedTypeId;
}

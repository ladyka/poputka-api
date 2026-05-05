package by.ladyka.poputka.data.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripCreateRequestDto {
    private OSMPlaceDto from;
    private OSMPlaceDto to;
    private Long startEpochMillis;
    private String description;
    private int passengers;
}


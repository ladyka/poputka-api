package by.ladyka.poputka.data.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OSMPlaceDto {
    private String name;
    private String city;
    private String displayName;
    private Long osm_id;
    private String osm_type;
    private Double lat;
    private Double lon;
}

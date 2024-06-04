package by.ladyka.poputka.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PoputkaTG_RideRequestDeleteDto extends PoputkaTG_RequestDto {
    @JsonProperty("ride_id")
    private int rideId;
}

package by.ladyka.poputka.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PoputkaTG_RideRequestDto extends PoputkaTG_RequestDto {
    @JsonProperty("ride_id")
    private int rideId;
    @JsonProperty("father_ride_id")
    private int fatherRideId;
    @JsonProperty("from_place_now")
    private String fromPlaceNow;
    @JsonProperty("to_place_now")
    private String toPlaceNow;
    @JsonProperty("day")
    private byte day;
    @JsonProperty("month_name")
    private String monthName;
    @JsonProperty("car")
    private String car;
    @JsonProperty("driver_wishes")
    private String driverWishes;
    @JsonProperty("time_start")
    private byte timeStart;
    @JsonProperty("user_nickname")
    private String userNickname;
}

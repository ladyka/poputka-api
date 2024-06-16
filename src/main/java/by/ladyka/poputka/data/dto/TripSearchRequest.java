package by.ladyka.poputka.data.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripSearchRequest {
    private String placeFrom;
    private String placeTo;
    //    private Pageable pageable;
}

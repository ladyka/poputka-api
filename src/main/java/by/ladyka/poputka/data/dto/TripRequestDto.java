package by.ladyka.poputka.data.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TripRequestDto {
    private Long id;
    private String from;
    private String to;
    //    private Instant start;
    private BigDecimal price;
    private String currency;
    private String car;
    private String description;
    private byte passengers;
    private String driverName;
}

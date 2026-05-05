package by.ladyka.poputka.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "trips")
public class TripEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String placeFrom;
    private String placeTo;
    private String placeFromCity;
    private String placeFromDisplayName;
    private Long placeFromOsmId;
    private String placeFromOsmType;
    private Double placeFromLat;
    private Double placeFromLon;

    private String placeToCity;
    private String placeToDisplayName;
    private Long placeToOsmId;
    private String placeToOsmType;
    private Double placeToLat;
    private Double placeToLon;
    private long start;
//    private BigDecimal price = BigDecimal.ZERO;
//    private String currency = "NONE";
    private String description;
    private byte passengers;

    private long ownerId;

    public Instant getStartTime() {
        return Instant.ofEpochMilli(start);
    }

    public void setStartTime(Instant startTime) {
        start = startTime.toEpochMilli();
    }
}

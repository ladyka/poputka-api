package by.ladyka.poputka.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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
    private long start;
    private BigDecimal price;
    private String currency;
    private String description;
    private byte passengers;

    private long ownerId;

    public Instant getStartTime() {
        return Instant.ofEpochSecond(start);
    }

    public void setStartTime(Instant startTime) {
        start = startTime.getEpochSecond();
    }
}

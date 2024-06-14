package by.ladyka.poputka.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
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
    private Long id;

    private String placeFrom;
    private String placeTo;
    private Long start;
    private BigDecimal price;
    private String currency;
    @Lob
    private String description;
    private byte passengers;

    @ManyToOne
    private PoputkaUser owner;

    public Instant getStartTime() {
        return Instant.ofEpochSecond(start);
    }

    public void setStartTime(Instant startTime) {
        start = startTime.getEpochSecond();
    }
}

package by.ladyka.poputka.data.entity;

import by.ladyka.poputka.data.enums.BookingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "booking")
public class Booking extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private String id;

    //    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    //    @JoinColumn(name = "trip_id", nullable = false)
    //    private TripEntity trip;

    @Column(name = "trip_id")
    private Long tripId;

    //    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    //    @JoinColumn(name = "passenger_id", nullable = false)
    //    private PoputkaUser passenger;

    @Column(name = "passenger_id")
    private Long passengerId;

    @Enumerated(value = EnumType.STRING)
    private BookingStatus bookingStatus;

}

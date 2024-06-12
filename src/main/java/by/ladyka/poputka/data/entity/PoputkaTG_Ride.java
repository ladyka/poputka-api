package by.ladyka.poputka.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "poputka_tg_rides")
public class PoputkaTG_Ride extends Auditable {
    @Id
    private long rideId;
    private long fatherRideId;
    private String fromPlaceNow;
    private String toPlaceNow;
    private byte day;
    private String monthName;
    private String car;
    private String driverWishes;
    private short price;
    private String valutaShort;
    private byte timeStart;
    private String userNickname;
}

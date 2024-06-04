package by.ladyka.poputka.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "poputka_tg_rides")
public class PoputkaTG_Ride {

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

    @CreatedBy
    private String createdBy;
    @CreatedDate
    private Instant createdDatetime;
    @LastModifiedBy
    private String modifiedBy;
    @LastModifiedDate
    private Instant modifiedDatetime;
    @Version
    private byte version;
}

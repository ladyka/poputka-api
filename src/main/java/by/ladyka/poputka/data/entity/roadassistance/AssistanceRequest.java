package by.ladyka.poputka.data.entity.roadassistance;

import by.ladyka.poputka.data.entity.Auditable;
import by.ladyka.poputka.data.entity.PoputkaUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "assistance_requests")
public class AssistanceRequest extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "car_info")
    private String carInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_type_id", nullable = false)
    private AssistanceType problemType;

    @Column(name = "description")
    private String description;

    @Column(name = "location_lat", nullable = false, precision = 10, scale = 8)
    private BigDecimal locationLat;

    @Column(name = "location_lon", nullable = false, precision = 11, scale = 8)
    private BigDecimal locationLon;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AssistanceRequestStatus status = AssistanceRequestStatus.CREATED;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "initial_expires_at", nullable = false)
    private Instant initialExpiresAt;

    @Column(name = "max_expires_at", nullable = false)
    private Instant maxExpiresAt;

    @Column(name = "last_activity_at", nullable = false)
    private Instant lastActivityAt = Instant.now();

    public PoputkaUser getAuthor() {
        return getCreatedUser();
    }
}

package by.ladyka.poputka.data.entity.roadassistance;

import by.ladyka.poputka.data.entity.Auditable;
import by.ladyka.poputka.data.entity.PoputkaUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "assistance_offers")
public class AssistanceOffer extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private AssistanceRequest request;

    @Column(name = "message")
    private String message;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AssistanceOfferStatus status = AssistanceOfferStatus.PENDING;

    @Column(name = "chat_id")
    private UUID chatId;

    public PoputkaUser getHelper() {
        return getCreatedUser();
    }
}

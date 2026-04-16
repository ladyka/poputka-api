package by.ladyka.poputka.data.entity;

import by.ladyka.poputka.data.enums.MessageStatus;
import by.ladyka.poputka.data.dto.payload.MessagePayload;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "message")
public class BookingMessage extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private String id;

    //    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    //    @JoinColumn(name = "booking_id", nullable = false)
    //    private Booking booking;

    @Column(name = "booking_id", nullable = false)
    private String bookingId;

    //    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    //    @JoinColumn(name = "sender_id", nullable = false)
    //    private PoputkaUser sender;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private MessagePayload payload;

    @Enumerated(value = EnumType.STRING)
    private MessageStatus messageStatus;

}

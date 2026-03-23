package by.ladyka.poputka.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) // ВАЖНО: для автоматического заполнения
public abstract class Auditable {

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_user", updatable = false)
    private PoputkaUser createdUser;

    @Column(name = "created_datetime", nullable = false)
    @CreatedDate
    private Long createdDatetime;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_user")
    private PoputkaUser modifiedUser;

    @Column(name = "modified_datetime", nullable = false)
    @LastModifiedDate
    private Long modifiedDatetime;

    @Column(name = "version", nullable = false)
    @Version
    private short version = -1;

    public Instant getCreated() {
        return Instant.ofEpochMilli(this.createdDatetime);
    }

    public Instant getModified() {
        return Instant.ofEpochMilli(this.modifiedDatetime);
    }
}

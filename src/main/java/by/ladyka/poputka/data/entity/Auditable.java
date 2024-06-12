package by.ladyka.poputka.data.entity;

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
public abstract class Auditable {
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

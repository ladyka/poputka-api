package by.ladyka.poputka.data.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public abstract class Auditable {
    //    @CreatedBy
    private String createdBy = "fix_me";
    //    @CreatedDate
    private Long createdDatetime = Instant.now().getEpochSecond();
    @LastModifiedBy
    private String modifiedBy;
    @LastModifiedDate
    private Instant modifiedDatetime;
    //    @Version
    private Byte version = (byte) -1;
}

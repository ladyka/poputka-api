package by.ladyka.poputka.data.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public abstract class Auditable {
    @CreatedBy
    private String createdUser = "fix_me";
    @CreatedDate
    private Long createdDatetime = -1L;
    @LastModifiedBy
    private String modifiedUser = "fix_me";
    @LastModifiedDate
    private Long modifiedDatetime = -1L;
    @Version
    private short version = -1;

    public void setCreated(PoputkaUser user) {
        createdUser = user.getUUID();
        createdDatetime = Instant.now().getEpochSecond();
        version = 0;
        setModified(user);
    }

    public void setModified(PoputkaUser user) {
        modifiedUser = user.getUUID();
        modifiedDatetime = Instant.now().getEpochSecond();
        version += 1;
    }
}

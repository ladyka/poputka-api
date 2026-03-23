package by.ladyka.poputka.data.entity.roadassistance;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class UserCompetencyId implements Serializable {
    private Long userId;
    private Long competencyId;
}

package by.ladyka.poputka.data.entity.roadassistance;

import by.ladyka.poputka.data.entity.Auditable;
import by.ladyka.poputka.data.entity.PoputkaUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_competencies")
@IdClass(UserCompetencyId.class)
public class UserCompetency {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "competency_id")
    private Long competencyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private PoputkaUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competency_id", insertable = false, updatable = false)
    private Competency competency;
}

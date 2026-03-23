package by.ladyka.poputka.data.repository.roadassistance;

import by.ladyka.poputka.data.entity.roadassistance.UserCompetency;
import by.ladyka.poputka.data.entity.roadassistance.UserCompetencyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserCompetencyRepository extends JpaRepository<UserCompetency, UserCompetencyId> {
    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
    
    @Query("SELECT c FROM Competency c JOIN UserCompetency uc ON c.id = uc.competencyId WHERE uc.userId = :userId")
    List<by.ladyka.poputka.data.entity.roadassistance.Competency> findCompetenciesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT u FROM by.ladyka.poputka.data.entity.PoputkaUser u JOIN UserCompetency uc ON u.id = uc.userId WHERE uc.competencyId = :competencyId")
    List<by.ladyka.poputka.data.entity.PoputkaUser> findUsersByCompetencyId(@Param("competencyId") Long competencyId);
}

package by.ladyka.poputka.service.roadassistance;

import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.roadassistance.Competency;
import by.ladyka.poputka.data.entity.roadassistance.UserCompetency;
import by.ladyka.poputka.data.entity.roadassistance.UserCompetencyId;
import by.ladyka.poputka.data.repository.roadassistance.CompetencyRepository;
import by.ladyka.poputka.data.repository.roadassistance.UserCompetencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCompetencyService {
    private final UserCompetencyRepository userCompetencyRepository;
    private final CompetencyRepository competencyRepository;

    @Transactional
    public void updateUserCompetencies(PoputkaUser user, List<Long> competencyIds) {
        // Удаляем все существующие компетенции пользователя
        userCompetencyRepository.deleteByUserId(user.getId());

        // Добавляем новые компетенции
        for (Long competencyId : competencyIds) {
            UserCompetency userCompetency = new UserCompetency();
            userCompetency.setUserId(user.getId());
            userCompetency.setCompetencyId(competencyId);
            userCompetencyRepository.save(userCompetency);
        }
    }

    public List<Competency> getUserCompetencies(PoputkaUser user) {
        return userCompetencyRepository.findCompetenciesByUserId(user.getId());
    }

    public List<PoputkaUser> getUsersByCompetency(Competency competency) {
        return userCompetencyRepository.findUsersByCompetencyId(competency.getId());
    }

    public List<Competency> getAllCompetencies() {
        return competencyRepository.findAll();
    }

    public List<Competency> getCompetenciesByType(Long typeId) {
        return competencyRepository.findByRelatedTypeId(typeId);
    }
}

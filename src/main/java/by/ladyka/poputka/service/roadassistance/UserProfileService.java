package by.ladyka.poputka.service.roadassistance;

import by.ladyka.poputka.data.dto.UserInfoDto;
import by.ladyka.poputka.data.dto.roadassistance.CompetencyDto;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.roadassistance.Competency;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import by.ladyka.poputka.service.mapper.UserMapper;
import by.ladyka.poputka.service.mapper.roadassistance.CompetencyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final PoputkaUserRepository poputkaUserRepository;
    private final UserCompetencyService userCompetencyService;
    private final CompetencyMapper competencyMapper;
    private final UserMapper userMapper;

    @Transactional
    public UserInfoDto updateUserProfile(PoputkaUser user, Boolean readyToHelp, Integer helpRadius, List<Long> competencyIds) {
        if (readyToHelp != null) {
            user.setReadyToHelp(readyToHelp);
        }
        if (helpRadius != null) {
            user.setHelpRadius(helpRadius);
        }
        user = poputkaUserRepository.save(user);
        
        // Обновляем компетенции пользователя, если они указаны
        if (competencyIds != null) {
            userCompetencyService.updateUserCompetencies(user, competencyIds);
        }
        
        return userMapper.toDto(user);
    }

    public List<CompetencyDto> getUserCompetenciesDto(PoputkaUser user) {
        List<Competency> competencies = userCompetencyService.getUserCompetencies(user);
        return competencyMapper.toDtoList(competencies);
    }

    public List<CompetencyDto> getAllCompetenciesDto() {
        List<Competency> competencies = userCompetencyService.getAllCompetencies();
        return competencyMapper.toDtoList(competencies);
    }
}

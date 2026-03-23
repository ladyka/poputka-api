package by.ladyka.poputka.controllers.roadassistance;

import by.ladyka.poputka.ApplicationUserDetails;
import by.ladyka.poputka.data.dto.UserInfoDto;
import by.ladyka.poputka.data.dto.roadassistance.CompetencyDto;
import by.ladyka.poputka.service.roadassistance.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roadassistance")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    @PutMapping("/profile")
    public ResponseEntity<UserInfoDto> updateProfile(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @RequestParam(required = false) Boolean readyToHelp,
            @RequestParam(required = false) Integer helpRadius,
            @RequestParam(required = false) List<Long> competencyIds) {
        
        UserInfoDto updatedUser = userProfileService.updateUserProfile(
            user.user(), readyToHelp, helpRadius, competencyIds);

        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/competencies")
    public ResponseEntity<List<CompetencyDto>> getAllCompetencies() {
        List<CompetencyDto> competencies = userProfileService.getAllCompetenciesDto();
        return ResponseEntity.ok(competencies);
    }

    @GetMapping("/profile/competencies")
    public ResponseEntity<List<CompetencyDto>> getUserCompetencies(@AuthenticationPrincipal ApplicationUserDetails user) {
        List<CompetencyDto> competencies = userProfileService.getUserCompetenciesDto(user.user());
        return ResponseEntity.ok(competencies);
    }
}

package by.ladyka.poputka.controllers;

import by.ladyka.poputka.data.dto.SingUpRequest;
import by.ladyka.poputka.data.dto.UserInfoDto;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final PoputkaUserRepository poputkaUserRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/singup")
    public Map<String, Boolean> createUser(@RequestBody SingUpRequest request) {
        PoputkaUser user = new PoputkaUser();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        PoputkaUser save = poputkaUserRepository.save(user);
        return Map.of("success", (save.getId() > 0));
    }

    @GetMapping("/info")
    public UserInfoDto userInfoDto(Principal principal) {
        if (Objects.isNull(principal)) {
            return null;
        }
        PoputkaUser user = poputkaUserRepository.findByUsername(principal.getName()).orElseThrow();
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setEmail(user.getEmail());
        userInfoDto.setName(user.getName());
        userInfoDto.setSurname(user.getSurname());
        userInfoDto.setBirthday(user.getBirthday());
        userInfoDto.setMusic(user.getMusic());
        userInfoDto.setBusinessActivity(user.getBusinessActivity());
        userInfoDto.setDescription(user.getDescription());
        userInfoDto.setCar(user.getCar());
        return userInfoDto;
    }
}

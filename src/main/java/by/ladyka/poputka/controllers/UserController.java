package by.ladyka.poputka.controllers;

import by.ladyka.poputka.data.dto.SingUpRequest;
import by.ladyka.poputka.data.dto.UserInfoDto;
import by.ladyka.poputka.data.dto.UserInfoSaveRequestDto;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import by.ladyka.poputka.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final PoputkaUserRepository poputkaUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

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

    @GetMapping("/currentAuth")
    public String userInfoDtoTest(Principal principal) {
        if (Objects.isNull(principal)) {
            log.error("Principal is null");
        }
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Hello, " + currentAuth.getName() + "! Your authorities are: " + currentAuth.getAuthorities());
        return currentAuth.getName();
    }

    @GetMapping("/info")
    public UserInfoDto userInfoDto(Principal principal) {
        if (Objects.isNull(principal)) {
            return new UserInfoDto();
        }
        PoputkaUser user = poputkaUserRepository.findByUsername(principal.getName()).orElseThrow();
        return userMapper.toDto(user);
    }

    @PostMapping("/update")
    public UserInfoDto userInfoDto(Principal principal, @RequestBody UserInfoSaveRequestDto dto) {
        if (Objects.isNull(principal)) {
            return new UserInfoDto();
        }
        PoputkaUser entity = poputkaUserRepository.findByUsername(principal.getName()).orElseThrow();
        PoputkaUser user = userMapper.updateEntity(dto, entity);
        PoputkaUser save = poputkaUserRepository.save(user);
        return userMapper.toDto(save);
    }
}

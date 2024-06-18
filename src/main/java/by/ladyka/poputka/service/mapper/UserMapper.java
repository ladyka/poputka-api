package by.ladyka.poputka.service.mapper;

import by.ladyka.poputka.data.dto.UserInfoDto;
import by.ladyka.poputka.data.dto.UserInfoSaveRequestDto;
import by.ladyka.poputka.data.entity.PoputkaUser;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {

    public UserInfoDto toDto(PoputkaUser user) {
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setEmail(user.getEmail());
        userInfoDto.setName(user.getName());
        userInfoDto.setSurname(user.getSurname());
        userInfoDto.setBirthday(user.getBirthday());
        userInfoDto.setMusic(user.getMusic());
        userInfoDto.setBusinessActivity(user.getBusinessActivity());
        userInfoDto.setDescription(user.getDescription());
        userInfoDto.setCar(user.getCar());
        userInfoDto.setTelegramId(user.getTelegramId());
        userInfoDto.setTelegramUsername(user.getTelegramUsername());
        return userInfoDto;
    }

    public PoputkaUser updateEntity(UserInfoSaveRequestDto dto, PoputkaUser entity) {
        entity.setName(dto.getName());
        entity.setSurname(dto.getSurname());
        entity.setBirthday(dto.getBirthday());
        entity.setMusic(dto.getMusic());
        entity.setBusinessActivity(dto.getBusinessActivity());
        entity.setDescription(dto.getDescription());
        entity.setCar(dto.getCar());
        return entity;
    }
}

package by.ladyka.poputka.data.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserInfoSaveRequestDto {
    private String name;
    private String surname;
    private LocalDate birthday;
    private String music;
    private String businessActivity;
    private String description;
    private String car;
}

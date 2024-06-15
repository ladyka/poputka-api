package by.ladyka.poputka.data.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SingUpRequest {
    private String email;
    private String password;
    private String name;
    private String surname;
}

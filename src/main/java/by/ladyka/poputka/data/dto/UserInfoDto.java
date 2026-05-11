package by.ladyka.poputka.data.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UserInfoDto {
    private String email;
    private String name;
    private String surname;
    private LocalDate birthday;
    private String music;
    private String businessActivity;
    private String description;
    private String car;
    private long telegramId;
    private String telegramUsername;

    // Поля для модуля помощи на дороге
    private Boolean readyToHelp = false;
    private BigDecimal rating = BigDecimal.ZERO;

    /** {@code true} если аккаунт связан с Google (есть сохранённый {@code google_sub}). */
    private boolean googleLinked;

    /** {@code true} если аккаунт связан с Apple (есть сохранённый {@code apple_sub}). */
    private boolean appleLinked;
}

package by.ladyka.poputka.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

/** Тело клиента после Sign in with Apple: JWT {@code identityToken} (можно отправить как {@code identity_token}). */
public record AppleAuthRequest(
        @NotBlank @JsonAlias("identity_token") String identityToken) {
}

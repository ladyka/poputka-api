package by.ladyka.poputka.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UnlinkAppleRequest(@NotBlank String password) {
}

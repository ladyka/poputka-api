package by.ladyka.poputka.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UnlinkGoogleRequest(@NotBlank String password) {
}

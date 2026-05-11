package by.ladyka.poputka.auth.dto;

/** Body refresh token optional when the HttpOnly cookie is sent. */
public record RefreshRequest(String refreshToken) {
}

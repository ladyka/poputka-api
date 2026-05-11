package by.ladyka.poputka.auth.dto;

import by.ladyka.poputka.data.dto.UserInfoDto;

/**
 * Tokens for clients: access JWT in memory; refresh token from JSON (mobile/native) or HttpOnly cookie (web).
 */
public record AuthTokensResponse(String accessToken, String refreshToken, UserInfoDto user) {
}

package by.ladyka.poputka.auth;

import by.ladyka.poputka.auth.dto.AuthTokensResponse;
import by.ladyka.poputka.auth.dto.AppleAuthRequest;
import by.ladyka.poputka.auth.dto.GoogleAuthRequest;
import by.ladyka.poputka.auth.dto.PasswordLoginRequest;
import by.ladyka.poputka.auth.dto.RefreshRequest;
import by.ladyka.poputka.auth.dto.UnlinkAppleRequest;
import by.ladyka.poputka.auth.dto.UnlinkGoogleRequest;
import by.ladyka.poputka.data.dto.UserInfoDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;
    private final RefreshTokenExtractor refreshTokenExtractor;

    @PostMapping("/apple")
    public ResponseEntity<AuthTokensResponse> apple(
            @Valid @RequestBody AppleAuthRequest body, HttpServletRequest request) {
        AuthTokensResponse tokens = authService.authenticateWithApple(body.identityToken(), request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieService.buildRefreshCookie(tokens.refreshToken()).toString())
                .body(tokens);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthTokensResponse> google(
            @Valid @RequestBody GoogleAuthRequest body, HttpServletRequest request) {
        AuthTokensResponse tokens = authService.authenticateWithGoogle(body.idToken(), request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieService.buildRefreshCookie(tokens.refreshToken()).toString())
                .body(tokens);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokensResponse> login(
            @Valid @RequestBody PasswordLoginRequest body, HttpServletRequest request) {
        AuthTokensResponse tokens = authService.loginWithPassword(body, request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieService.buildRefreshCookie(tokens.refreshToken()).toString())
                .body(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokensResponse> refresh(
            @RequestBody(required = false) RefreshRequest body, HttpServletRequest request) {
        String refresh =
                refreshTokenExtractor.extract(body, request).orElseThrow(() -> new ResponseStatusException(
                        UNAUTHORIZED, "Refresh token missing (cookie or body)"));
        AuthTokensResponse tokens = authService.refresh(refresh, request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieService.buildRefreshCookie(tokens.refreshToken()).toString())
                .body(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) RefreshRequest body, HttpServletRequest request) {
        refreshTokenExtractor.extract(body, request).ifPresent(authService::logout);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authCookieService.clearRefreshCookie().toString())
                .build();
    }

    @PostMapping("/apple/link")
    @PreAuthorize("isAuthenticated()")
    public UserInfoDto linkApple(@Valid @RequestBody AppleAuthRequest body, Principal principal) {
        return authService.linkAppleAccount(principal.getName(), body.identityToken());
    }

    @PostMapping("/apple/unlink")
    @PreAuthorize("isAuthenticated()")
    public UserInfoDto unlinkApple(@Valid @RequestBody UnlinkAppleRequest body, Principal principal) {
        return authService.unlinkAppleAccount(principal.getName(), body.password());
    }

    @PostMapping("/google/link")
    @PreAuthorize("isAuthenticated()")
    public UserInfoDto linkGoogle(@Valid @RequestBody GoogleAuthRequest body, Principal principal) {
        return authService.linkGoogleAccount(principal.getName(), body.idToken());
    }

    @PostMapping("/google/unlink")
    @PreAuthorize("isAuthenticated()")
    public UserInfoDto unlinkGoogle(@Valid @RequestBody UnlinkGoogleRequest body, Principal principal) {
        return authService.unlinkGoogleAccount(principal.getName(), body.password());
    }
}

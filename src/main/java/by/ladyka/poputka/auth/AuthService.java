package by.ladyka.poputka.auth;

import by.ladyka.poputka.ApplicationUserDetails;
import by.ladyka.poputka.auth.dto.AuthTokensResponse;
import by.ladyka.poputka.auth.dto.PasswordLoginRequest;
import by.ladyka.poputka.data.dto.UserInfoDto;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import by.ladyka.poputka.service.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleTokenVerificationService googleTokenVerificationService;
    private final AppleTokenVerificationService appleTokenVerificationService;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final PoputkaUserRepository poputkaUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Transactional
    public AuthTokensResponse authenticateWithGoogle(String idToken, HttpServletRequest request) {
        GoogleUserPrincipal google = googleTokenVerificationService.verify(idToken);
        PoputkaUser user =
                resolveByGoogleSub(google).or(() -> maybeAutoLinkExistingUser(google)).orElseGet(() -> registerNewGoogleUser(google));
        return buildAuthTokens(user, request);
    }

    @Transactional
    public AuthTokensResponse authenticateWithApple(String identityToken, HttpServletRequest request) {
        AppleUserPrincipal apple = appleTokenVerificationService.verify(identityToken);
        PoputkaUser user =
                resolveByAppleSub(apple).or(() -> maybeAutoLinkExistingUserApple(apple)).orElseGet(() -> registerNewAppleUser(apple));
        return buildAuthTokens(user, request);
    }

    private Optional<PoputkaUser> resolveByGoogleSub(GoogleUserPrincipal google) {
        return poputkaUserRepository.findByGoogleSub(google.sub());
    }

    private Optional<PoputkaUser> maybeAutoLinkExistingUser(GoogleUserPrincipal google) {
        if (!google.emailVerified()) {
            return Optional.empty();
        }
        Optional<PoputkaUser> byEmail = poputkaUserRepository.findByUsername(normalizeEmail(google.email()));
        return byEmail.map(user -> enforceLinkSubAndEmail(user, google));
    }

    private PoputkaUser enforceLinkSubAndEmail(PoputkaUser user, GoogleUserPrincipal google) {
        if (user.getGoogleSub() != null && !user.getGoogleSub().equals(google.sub())) {
            throw new ResponseStatusException(CONFLICT, "Google account differs from linked identity");
        }
        user.setGoogleSub(google.sub());
        user.setGoogleEmail(google.email());
        return poputkaUserRepository.save(user);
    }

    private Optional<PoputkaUser> resolveByAppleSub(AppleUserPrincipal apple) {
        return poputkaUserRepository.findByAppleSub(apple.sub());
    }

    private Optional<PoputkaUser> maybeAutoLinkExistingUserApple(AppleUserPrincipal apple) {
        if (!AppleUserPrincipal.emailPresent(apple.email()) || !apple.emailVerified()) {
            return Optional.empty();
        }
        Optional<PoputkaUser> byEmail = poputkaUserRepository.findByUsername(normalizeEmail(apple.email()));
        return byEmail.map(user -> enforceLinkAppleSubAndEmail(user, apple));
    }

    private PoputkaUser enforceLinkAppleSubAndEmail(PoputkaUser user, AppleUserPrincipal apple) {
        if (user.getAppleSub() != null && !user.getAppleSub().equals(apple.sub())) {
            throw new ResponseStatusException(CONFLICT, "Apple account differs from linked identity");
        }
        user.setAppleSub(apple.sub());
        user.setAppleEmail(apple.email());
        return poputkaUserRepository.save(user);
    }

    private PoputkaUser registerNewAppleUser(AppleUserPrincipal apple) {
        if (!apple.emailVerified() || !AppleUserPrincipal.emailPresent(apple.email())) {
            throw new ResponseStatusException(FORBIDDEN,
                    "Apple identity token must include a verified email to create an account (use «Share My Email» on first sign-in)");
        }
        String email = normalizeEmail(apple.email());
        if (poputkaUserRepository.findByUsername(email).isPresent()) {
            throw new ResponseStatusException(CONFLICT,
                    "An account already exists with this email. Sign in with password or use Apple «Share My Email» to link.");
        }
        PoputkaUser u = new PoputkaUser();
        u.setEmail(email);
        u.setAppleSub(apple.sub());
        u.setAppleEmail(apple.email());
        u.setName(extractGuessName(email));
        u.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        return poputkaUserRepository.save(u);
    }

    private PoputkaUser registerNewGoogleUser(GoogleUserPrincipal google) {
        if (!google.emailVerified()) {
            throw new ResponseStatusException(FORBIDDEN, "Google email must be verified to create an account");
        }
        String email = normalizeEmail(google.email());
        if (poputkaUserRepository.findByUsername(email).isPresent()) {
            throw new ResponseStatusException(CONFLICT,
                    "An account already exists with this email. Sign in with password or verify your Google email to link.");
        }
        PoputkaUser u = new PoputkaUser();
        u.setEmail(email);
        u.setGoogleSub(google.sub());
        u.setGoogleEmail(google.email());
        u.setName(extractGuessName(email));
        u.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        return poputkaUserRepository.save(u);
    }

    public AuthTokensResponse loginWithPassword(PasswordLoginRequest request, HttpServletRequest httpReq) {
        UsernamePasswordAuthenticationToken authReq =
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password());
        Authentication auth = authenticationManager.authenticate(authReq);
        PoputkaUser user = unwrapUser(auth.getPrincipal());
        return buildAuthTokens(user, httpReq);
    }

    @Transactional
    public AuthTokensResponse refresh(String refreshPlaintext, HttpServletRequest request) {
        RefreshTokenService.RotatedRefresh rotated =
                refreshTokenService.rotateReturningUser(refreshPlaintext, clientUserAgent(request), clientIp(request));
        String access =
                jwtTokenService.createAccessToken(rotated.user().getUsername(), rotated.user().getId());
        return new AuthTokensResponse(access, rotated.refreshPlain(), userMapper.toDto(rotated.user()));
    }

    public void logout(String refreshPlaintext) {
        refreshTokenService.revokeByPlaintext(refreshPlaintext);
    }

    @Transactional
    public UserInfoDto linkGoogleAccount(String authenticatedUsername, String idToken) {
        PoputkaUser user =
                poputkaUserRepository.findByUsername(authenticatedUsername).orElseThrow();
        if (user.getGoogleSub() != null) {
            throw new ResponseStatusException(CONFLICT, "Google account already linked");
        }
        GoogleUserPrincipal google = googleTokenVerificationService.verify(idToken);
        if (!google.emailVerified()) {
            throw new ResponseStatusException(FORBIDDEN, "Google email must be verified");
        }
        if (!normalizeEmail(user.getUsername()).equals(normalizeEmail(google.email()))) {
            throw new ResponseStatusException(FORBIDDEN, "Google email must match the signed-in account");
        }
        poputkaUserRepository.findByGoogleSub(google.sub()).ifPresent(other -> {
            if (other.getId() != user.getId()) {
                throw new ResponseStatusException(CONFLICT, "This Google account is linked to another user");
            }
        });
        user.setGoogleSub(google.sub());
        user.setGoogleEmail(google.email());
        return userMapper.toDto(poputkaUserRepository.save(user));
    }

    @Transactional
    public UserInfoDto linkAppleAccount(String authenticatedUsername, String identityToken) {
        PoputkaUser user =
                poputkaUserRepository.findByUsername(authenticatedUsername).orElseThrow();
        if (user.getAppleSub() != null) {
            throw new ResponseStatusException(CONFLICT, "Apple account already linked");
        }
        AppleUserPrincipal apple = appleTokenVerificationService.verify(identityToken);
        if (!AppleUserPrincipal.emailPresent(apple.email())) {
            throw new ResponseStatusException(FORBIDDEN, "Apple identity token must include email to link");
        }
        if (!apple.emailVerified()) {
            throw new ResponseStatusException(FORBIDDEN, "Apple email must be verified");
        }
        if (!normalizeEmail(user.getUsername()).equals(normalizeEmail(apple.email()))) {
            throw new ResponseStatusException(FORBIDDEN, "Apple email must match the signed-in account");
        }
        poputkaUserRepository.findByAppleSub(apple.sub()).ifPresent(other -> {
            if (other.getId() != user.getId()) {
                throw new ResponseStatusException(CONFLICT, "This Apple account is linked to another user");
            }
        });
        user.setAppleSub(apple.sub());
        user.setAppleEmail(apple.email());
        return userMapper.toDto(poputkaUserRepository.save(user));
    }

    @Transactional
    public UserInfoDto unlinkAppleAccount(String authenticatedUsername, String password) {
        PoputkaUser user =
                poputkaUserRepository.findByUsername(authenticatedUsername).orElseThrow();
        if (user.getAppleSub() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Apple account is not linked");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid password");
        }
        user.setAppleSub(null);
        user.setAppleEmail(null);
        return userMapper.toDto(poputkaUserRepository.save(user));
    }

    @Transactional
    public UserInfoDto unlinkGoogleAccount(String authenticatedUsername, String password) {
        PoputkaUser user =
                poputkaUserRepository.findByUsername(authenticatedUsername).orElseThrow();
        if (user.getGoogleSub() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Google account is not linked");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid password");
        }
        user.setGoogleSub(null);
        user.setGoogleEmail(null);
        return userMapper.toDto(poputkaUserRepository.save(user));
    }

    private AuthTokensResponse buildAuthTokens(PoputkaUser user, HttpServletRequest request) {
        String refresh = refreshTokenService.issue(user, clientUserAgent(request), clientIp(request));
        String access = jwtTokenService.createAccessToken(user.getUsername(), user.getId());
        return new AuthTokensResponse(access, refresh, userMapper.toDto(user));
    }

    private static PoputkaUser unwrapUser(Object principal) {
        if (principal instanceof ApplicationUserDetails details) {
            return details.user();
        }
        throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static String extractGuessName(String email) {
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    private static String clientUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

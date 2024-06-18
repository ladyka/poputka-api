package by.ladyka.poputka.controllers;

import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping(TelegramController.API_TELEGRAM)
@RequiredArgsConstructor
public class TelegramController {

    public static final String API_TELEGRAM = "/api/telegram";
    private static final String TELEGRAM_TOKEN = "7083532200:AAEf4rz27a9ev9VXVFE8RdffNzSucnl4VRc";
    private final PoputkaUserRepository poputkaUserRepository;

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody String telegram(
            long id,
            String first_name,
            String last_name,
            String username,
            String photo_url,
            String auth_date,
            String hash,
            Principal principal,
            HttpServletRequest request,
            HttpServletResponse response
                                        ) throws Exception {

        Map<String, Object> authData = new HashMap<>();
        authData.put("id", id);
        if (Objects.nonNull(first_name)) {
            authData.put("first_name", first_name);
        }
        if (Objects.nonNull(last_name)) {
            authData.put("last_name", last_name);
        }
        if (Objects.nonNull(username)) {
            authData.put("username", username);
        }
        if (Objects.nonNull(photo_url)) {
            authData.put("photo_url", photo_url);
        }
        if (Objects.nonNull(auth_date)) {
            authData.put("auth_date", auth_date);
        }
        authData.put("hash", hash);

        if (checkTelegramAuthorization(authData)) {
            PoputkaUser user;
            Optional<PoputkaUser> byTelegramId = poputkaUserRepository.findByTelegramId(id);
            if (principal != null) {
                user = poputkaUserRepository.findByUsername(principal.getName()).orElseThrow();
                if (byTelegramId.isPresent()) {
                    PoputkaUser userTg = byTelegramId.get();
                    if (!Objects.equals(userTg.getId(), user.getId())) {
                        throw new RuntimeException("Нельзя привязывать один телеграмм аккаунт к 2 учётным записям!");
                    }
                }
            } else {
                user = byTelegramId.orElseGet(() -> {
                    PoputkaUser poputkaUser = new PoputkaUser();
                    poputkaUser.setUsername(UUID.randomUUID().toString());
                    poputkaUser.setPassword(UUID.randomUUID().toString());
                    return poputkaUser;
                });
                // Авторизуем пользователя
                AnonymousAuthenticationToken token = new AnonymousAuthenticationToken(
                        "anonymous", user.getUsername(), user.getAuthorities());
                SecurityContext context = SecurityContextHolder.getContext();
                context.setAuthentication(token);
            }
            user.setName(first_name);
            user.setSurname(last_name);
            user.setTelegramId(id);
            user.setTelegramUsername(username);
            poputkaUserRepository.save(user);
        }

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta http-equiv=\"refresh\" content=\"0;url=/profile\">" +
                "</head>" +
                "<body>" +
                "Hello, " + currentAuth.getName() + "! Your authorities are: " + currentAuth.getAuthorities() +
                "</body>" +
                "</html>";
    }

    public boolean checkTelegramAuthorization(Map<String, Object> request) {
        String hash = (String) request.get("hash");
        request.remove("hash");

        // Prepare the string
        String str = request.entrySet().stream()
                .sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()))
                .map(kvp -> kvp.getKey() + "=" + kvp.getValue())
                .collect(Collectors.joining("\n"));

        try {
            SecretKeySpec sk = new SecretKeySpec(
                    // Get SHA 256 from telegram token
                    MessageDigest.getInstance("SHA-256").digest(TELEGRAM_TOKEN.getBytes(StandardCharsets.UTF_8)
                                                               ), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(sk);

            byte[] result = mac.doFinal(str.getBytes(StandardCharsets.UTF_8));

            // Convert the result to hex string

            String resultStr = HexUtils.toHexString(result);

            // Compare the result with the hash from body
            // Do other things like create a user and JWT token
            boolean isOk = hash.compareToIgnoreCase(resultStr) == 0;
            return isOk;
        } catch (Exception e) {
            return false;
        }
    }
}

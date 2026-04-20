package by.ladyka.poputka.service;

import by.ladyka.poputka.ApplicationUserDetails;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TelegramService {

    @Value("{telegram-token}")
    private String telegramToken;
    private final PoputkaUserRepository poputkaUserRepository;

    public void telegram(long id,
            String first_name,
            String last_name,
            String username,
            String photo_url,
            String auth_date,
            String hash,
            ApplicationUserDetails applicationUserDetails) {

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
            if (applicationUserDetails != null) {
                user = applicationUserDetails.user();
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
                    MessageDigest.getInstance("SHA-256").digest(telegramToken.getBytes(StandardCharsets.UTF_8)
                                                               ), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(sk);

            byte[] result = mac.doFinal(str.getBytes(StandardCharsets.UTF_8));

            // Convert the result to hex string

            String resultStr = HexUtils.toHexString(result);

            // Compare the result with the hash from body
            // Do other things like create a user and JWT token
            return hash.compareToIgnoreCase(resultStr) == 0;
        } catch (Exception e) {
            return false;
        }
    }
}

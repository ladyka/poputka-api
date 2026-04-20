package by.ladyka.poputka.service.notification;

import by.ladyka.poputka.config.NotificationConfig;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramNotificationService {

    private static final HttpClient TELEGRAM_HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final NotificationConfig notificationConfig;

    public void sendAdminUserRegistered(PoputkaUser user) {
        if (notificationConfig.getAdmin().getTelegramChatId() == null) {
            log.warn("Telegram chat ID not configured for admin notifications");
            return;
        }

        String message = String.format(
                """
                📢 Новый пользователь зарегистрирован!
                
                Имя: %s %s
                Email: %s
                Телеграм: %s""",
            user.getName(),
            user.getSurname(),
            user.getEmail(),
            user.getTelegramUsername() != null ? "@" + user.getTelegramUsername() : "Не привязан"
        );

        sendTelegramMessage(message);
    }

    public void sendAdminTripCreated(TripEntity trip, PoputkaUser owner) {
        if (notificationConfig.getAdmin().getTelegramChatId() == null) {
            log.warn("Telegram chat ID not configured for admin notifications");
            return;
        }

        String ownerInfo = owner != null ?
            String.format("%s %s (%s)", owner.getName(), owner.getSurname(), owner.getEmail()) :
            "Неизвестный пользователь";

        String message = String.format(
                """
                📢 Новая поездка создана!
                
                Откуда: %s
                Куда: %s
                Когда: %s
                Пассажиров: %d
                Описание: %s
                Создал: %s""",
            trip.getPlaceFrom(),
            trip.getPlaceTo(),
            java.time.Instant.ofEpochMilli(trip.getStart()).toString(),
            trip.getPassengers(),
            trip.getDescription() != null ? trip.getDescription() : "Нет описания",
            ownerInfo
        );

        sendTelegramMessage(message);
    }

    private void sendTelegramMessage(String message) {
        String token = notificationConfig.getAdmin().getTelegramCredentials();
        String chatId = notificationConfig.getAdmin().getTelegramChatId();
        if (token == null || token.isBlank()) {
            log.warn("Telegram bot token not configured (TELEGRAM_CREDENTIALS)");
            return;
        }
        if (chatId == null || chatId.isBlank()) {
            log.warn("Telegram chat ID not configured (TELEGRAM_CHAT_ID)");
            return;
        }

        try {
            String url = "https://api.telegram.org/bot" + token + "/sendMessage";
            String body = "chat_id=" + URLEncoder.encode(chatId, StandardCharsets.UTF_8)
                    + "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = TELEGRAM_HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Telegram API HTTP {} for chat_id={}: {}", response.statusCode(), chatId, response.body());
                return;
            }
            if (!response.body().contains("\"ok\":true")) {
                log.error("Telegram API rejected message for chat_id={}: {}", chatId, response.body());
                return;
            }
            log.info("Telegram message sent successfully to chat_id={}", chatId);
        } catch (Exception e) {
            log.error("Failed to send Telegram message", e);
        }
    }
}

package by.ladyka.poputka.service.notification;

import by.ladyka.poputka.config.NotificationConfig;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripEntity;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {
    private final NotificationConfig notificationConfig;

    @Qualifier("ladyka")
    private final JavaMailSender mailSender;
    private final PoputkaUserRepository poputkaUserRepository;

    public void sendAdminUserRegistered(PoputkaUser user) {
        if (notificationConfig.getAdmin().getAdminEmail() == null) {
            log.warn("Admin email not configured for notifications");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notificationConfig.getAdmin().getAdminEmail());
        message.setSubject("Новый пользователь зарегистрирован");
        message.setText(String.format(
                """
                Пользователь %d %s %s зарегистрировался в системе.
                
                Email: %s
                Телеграм: %s""",
            user.getId(),
            user.getName(),
            user.getSurname(),
            user.getEmail(),
            user.getTelegramUsername() != null ? "@" + user.getTelegramUsername() : "Не привязан"
        ));

        sendEmail(message);
    }

    public void sendAdminTripCreated(TripEntity trip, PoputkaUser owner) {
        if (notificationConfig.getAdmin().getAdminEmail() == null) {
            log.warn("Admin email not configured for notifications");
            return;
        }

        String ownerInfo = owner != null ?
            String.format("%s %s (%s)", owner.getName(), owner.getSurname(), owner.getEmail()) :
            "Неизвестный пользователь";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notificationConfig.getAdmin().getAdminEmail());
        message.setSubject("Новая поездка создана");
        message.setText(String.format(
                """
                Поездка %d из %s в %s в %s
                
                Пассажиров: %d
                Описание: %s
                Создал: %s""",
            trip.getId(),
            trip.getPlaceFrom(),
            trip.getPlaceTo(),
            Instant.ofEpochMilli(trip.getStart()).toString(),
            trip.getPassengers(),
            trip.getDescription() != null ? trip.getDescription() : "Нет описания",
            ownerInfo
        ));

        sendEmail(message);
    }

    public void sendUserMessagesSummary(long userId, List<String> messages) {
        PoputkaUser user = poputkaUserRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found with id: {}", userId);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Новые личные сообщения");
        message.setText(String.format(
            "У вас есть новые личные сообщения:\n\n%s\n\n" +
            "Вы можете ответить на них в личном кабинете.",
            String.join("\n\n", messages)
        ));

        sendEmail(message);
    }

    private void sendEmail(SimpleMailMessage message) {
        try {
            mailSender.send(message);
            log.info("Email sent successfully to: {}", message.getTo()[0]);
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }
    }
}

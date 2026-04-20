package by.ladyka.poputka.service.notification;

import by.ladyka.poputka.config.NotificationConfig;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripEntity;
import by.ladyka.poputka.data.repository.BookingMessageRepository;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationConfig notificationConfig;
    private final PoputkaUserRepository poputkaUserRepository;
    private final TelegramNotificationService telegramNotificationService;
    private final EmailNotificationService emailNotificationService;

    // Store pending user messages for delayed email notifications
    private final ConcurrentMap<Long, List<String>> pendingUserMessages = new ConcurrentHashMap<>();

    @Override
    @Async
    public void sendAdminUserRegisteredNotification(PoputkaUser user) {
        if (!notificationConfig.getAdmin().isEnabled()) {
            return;
        }

        log.info("Sending admin notification about new user registration: {}", user.getEmail());

        if (notificationConfig.getAdmin().getChannels().contains("telegram")) {
            telegramNotificationService.sendAdminUserRegistered(user);
        }

        if (notificationConfig.getAdmin().getChannels().contains("email")) {
            emailNotificationService.sendAdminUserRegistered(user);
        }
    }

    @Override
    @Async
    public void sendAdminTripCreatedNotification(TripEntity trip) {
        if (!notificationConfig.getAdmin().isEnabled()) {
            return;
        }

        PoputkaUser owner = poputkaUserRepository.findById(trip.getOwnerId()).orElse(null);

        log.info("Sending admin notification about new trip creation: {}", trip.getId());

        if (notificationConfig.getAdmin().getChannels().contains("telegram")) {
            telegramNotificationService.sendAdminTripCreated(trip, owner);
        }

        if (notificationConfig.getAdmin().getChannels().contains("email")) {
            emailNotificationService.sendAdminTripCreated(trip, owner);
        }
    }

    @Scheduled(fixedRate = 60000) // Every minute
    public void sendDelayedEmailNotifications() {
        if (!notificationConfig.getUser().isEnabled()) {
            return;
        }

        long cutoffTime = Instant.now().minusSeconds(
            notificationConfig.getUser().getEmailDelayMinutes() * 60
        ).getEpochSecond();

        pendingUserMessages.forEach((userId, messages) -> {
            // Check if messages are older than the delay period
            boolean shouldSend = messages.stream()
                .anyMatch(msg -> Instant.parse(msg).getEpochSecond() <= cutoffTime);

            if (shouldSend) {
                emailNotificationService.sendUserMessagesSummary(userId, messages);
                pendingUserMessages.remove(userId);
            }
        });
    }
}

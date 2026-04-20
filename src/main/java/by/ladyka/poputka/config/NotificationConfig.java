package by.ladyka.poputka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "notification")
public class NotificationConfig {
    private AdminNotifications admin = new AdminNotifications();
    private UserNotifications user = new UserNotifications();

    public static class AdminNotifications {
        private boolean enabled = true;
        private List<String> channels = List.of("telegram", "email");
        private String telegramChatId;
        /** Bot token for Telegram Bot API ({@code id:secret}), env {@code TELEGRAM_CREDENTIALS}. */
        private String telegramCredentials;
        private String adminEmail;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getChannels() {
            return channels;
        }

        public void setChannels(List<String> channels) {
            this.channels = channels;
        }

        public String getTelegramChatId() {
            return telegramChatId;
        }

        public void setTelegramChatId(String telegramChatId) {
            this.telegramChatId = telegramChatId;
        }

        public String getTelegramCredentials() {
            return telegramCredentials;
        }

        public void setTelegramCredentials(String telegramCredentials) {
            this.telegramCredentials = telegramCredentials;
        }

        public String getAdminEmail() {
            return adminEmail;
        }

        public void setAdminEmail(String adminEmail) {
            this.adminEmail = adminEmail;
        }
    }

    public static class UserNotifications {
        private boolean enabled = true;
        private int emailDelayMinutes = 10;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getEmailDelayMinutes() {
            return emailDelayMinutes;
        }

        public void setEmailDelayMinutes(int emailDelayMinutes) {
            this.emailDelayMinutes = emailDelayMinutes;
        }
    }

    public AdminNotifications getAdmin() {
        return admin;
    }

    public void setAdmin(AdminNotifications admin) {
        this.admin = admin;
    }

    public UserNotifications getUser() {
        return user;
    }

    public void setUser(UserNotifications user) {
        this.user = user;
    }
}

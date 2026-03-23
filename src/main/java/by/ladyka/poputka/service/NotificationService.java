package by.ladyka.poputka.service;

import by.ladyka.poputka.data.entity.PoputkaUser;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    public void sendNotificationToUser(PoputkaUser author, String newOffer, String новоеПредложениеПомощи, String string) {

    }

    public void sendNotificationToAllReadyHelpers(String newAssistanceRequest, String новыйЗапросОПомощиРядом, String string) {

    }

    public void sendNotificationToChatParticipants(Long id, String requestExpiringSoon, String запросОПомощиПродлен) {

    }

    public void sendNotificationToModerators(String lowRatedUser, String s, long id) {

    }
}

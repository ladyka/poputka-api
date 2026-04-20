package by.ladyka.poputka.service.notification;

import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripEntity;

public interface NotificationService {
    void sendAdminUserRegisteredNotification(PoputkaUser user);
    void sendAdminTripCreatedNotification(TripEntity trip);
}

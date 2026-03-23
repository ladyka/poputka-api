package by.ladyka.poputka.service.roadassistance;

import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceOffer;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceOfferStatus;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceRequest;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceRequestStatus;
import by.ladyka.poputka.data.repository.roadassistance.AssistanceOfferRepository;
import by.ladyka.poputka.data.repository.roadassistance.AssistanceRequestRepository;
import by.ladyka.poputka.service.ChatService;
import by.ladyka.poputka.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssistanceOfferService {
    private final AssistanceOfferRepository assistanceOfferRepository;
    private final AssistanceRequestRepository assistanceRequestRepository;
    private final ChatService chatService;
    private final NotificationService notificationService;

    @Transactional
    public AssistanceOffer createOffer(PoputkaUser helper, Long requestId, String message) {
        // Проверка, что пользователь не заблокирован и имеет рейтинг >= 3.0
        if (helper.getIsBlocked() != null && helper.getIsBlocked()) {
            throw new IllegalStateException("Пользователь заблокирован");
        }
        
        if (helper.getRating() != null && helper.getRating().compareTo(java.math.BigDecimal.valueOf(3.0)) < 0) {
            throw new IllegalStateException("Рейтинг пользователя ниже 3.0");
        }

        AssistanceRequest request = assistanceRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Запрос не найден"));

        // Проверка статуса запроса
        if (!Arrays.asList(AssistanceRequestStatus.CREATED, AssistanceRequestStatus.OFFER_RECEIVED)
                .contains(request.getStatus())) {
            throw new IllegalStateException("Невозможно создать предложение для запроса в текущем статусе");
        }

        // Проверка, что пользователь не является автором запроса
        if (Objects.equals(request.getAuthor().getId(), helper.getId())) {
            throw new IllegalStateException("Нельзя помочь самому себе");
        }

        // Проверка, что пользователь еще не предлагал помощь по этому запросу
        Optional<AssistanceOffer> existingOffer = assistanceOfferRepository
            .findByRequestIdAndCreatedUser(requestId, helper);
        
        if (existingOffer.isPresent()) {
            throw new IllegalStateException("Вы уже предложили помощь по этому запросу");
        }

        // Создание предложения. Helper тот, от чего имени создана сущность
        AssistanceOffer offer = new AssistanceOffer();
        offer.setRequest(request);
        offer.setMessage(message);
        offer.setStatus(AssistanceOfferStatus.PENDING);
        
        // Создание чата типа ASSISTANCE
        UUID chatId = chatService.createAssistanceChat(request, helper);
        offer.setChatId(chatId);
        
        offer = assistanceOfferRepository.save(offer);

        // Обновление статуса запроса
        if (request.getStatus() == AssistanceRequestStatus.CREATED) {
            request.setStatus(AssistanceRequestStatus.OFFER_RECEIVED);
            assistanceRequestRepository.save(request);
        }

        // Уведомление автора запроса
        notificationService.sendNotificationToUser(
            request.getAuthor(), 
            "NEW_OFFER", 
            "Новое предложение помощи", 
            requestId.toString()
        );

        return offer;
    }

    @Transactional
    public AssistanceOffer cancelOffer(PoputkaUser user, Long offerId) {
        AssistanceOffer offer = assistanceOfferRepository.findById(offerId)
            .orElseThrow(() -> new IllegalArgumentException("Предложение не найдено"));

        // Проверка прав доступа
        if (!Objects.equals(offer.getHelper().getId(), user.getId())) {
            throw new IllegalStateException("Только автор предложения может его отменить");
        }

        // Проверка статуса
        if (offer.getStatus() != AssistanceOfferStatus.PENDING) {
            throw new IllegalStateException("Невозможно отменить предложение в текущем статусе");
        }

        offer.setStatus(AssistanceOfferStatus.CANCELLED);
        offer = assistanceOfferRepository.save(offer);

        // Автоматический отзыв при отмене после 15 минут
        if (Instant.now().isAfter(offer.getCreated().plusSeconds(15 * 60))) {
            // TODO: Создать автоматический отзыв
            // createAutoReview(offer);
        }

        return offer;
    }

    @Transactional
    public AssistanceOffer acceptOffer(PoputkaUser user, Long requestId, Long offerId) {
        AssistanceRequest request = assistanceRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Запрос не найден"));

        // Проверка прав доступа
        if (!Objects.equals(request.getAuthor().getId(), user.getId())) {
            throw new IllegalStateException("Только автор запроса может принять предложение");
        }

        // Проверка статуса запроса
        if (request.getStatus() != AssistanceRequestStatus.OFFER_RECEIVED) {
            throw new IllegalStateException("Невозможно принять предложение в текущем статусе запроса");
        }

        AssistanceOffer offer = assistanceOfferRepository.findById(offerId)
            .orElseThrow(() -> new IllegalArgumentException("Предложение не найдено"));

        // Проверка статуса предложения
        if (offer.getStatus() != AssistanceOfferStatus.PENDING) {
            throw new IllegalStateException("Невозможно принять предложение в текущем статусе");
        }

        // Обновление статуса принятого предложения
        offer.setStatus(AssistanceOfferStatus.ACCEPTED);
        assistanceOfferRepository.save(offer);

        // Обновление статусов всех других предложений на этот запрос
        List<AssistanceOffer> otherOffers = assistanceOfferRepository
            .findByRequestIdAndStatus(requestId, AssistanceOfferStatus.PENDING);
        
        for (AssistanceOffer otherOffer : otherOffers) {
            if (!otherOffer.getId().equals(offerId)) {
                otherOffer.setStatus(AssistanceOfferStatus.REJECTED);
                assistanceOfferRepository.save(otherOffer);
                
                // Уведомление других помощников
                notificationService.sendNotificationToUser(
                    otherOffer.getHelper(),
                    "OFFER_REJECTED",
                    "Ваше предложение помощи отклонено",
                    requestId.toString()
                );
            }
        }

        // Обновление статуса запроса
        request.setStatus(AssistanceRequestStatus.OFFER_ACCEPTED);
        assistanceRequestRepository.save(request);

        // Уведомление принятого помощника
        notificationService.sendNotificationToUser(
            offer.getHelper(),
            "OFFER_ACCEPTED",
            "Ваше предложение помощи принято",
            requestId.toString()
        );

        return offer;
    }

    @Transactional
    public AssistanceOffer rejectOffer(PoputkaUser user, Long requestId, Long offerId) {
        AssistanceRequest request = assistanceRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Запрос не найден"));

        // Проверка прав доступа
        if (!Objects.equals(request.getAuthor().getId(), user.getId())) {
            throw new IllegalStateException("Только автор запроса может отклонить предложение");
        }

        AssistanceOffer offer = assistanceOfferRepository.findById(offerId)
            .orElseThrow(() -> new IllegalArgumentException("Предложение не найдено"));

        // Проверка статуса предложения
        if (offer.getStatus() != AssistanceOfferStatus.PENDING) {
            throw new IllegalStateException("Невозможно отклонить предложение в текущем статусе");
        }

        offer.setStatus(AssistanceOfferStatus.REJECTED);
        offer = assistanceOfferRepository.save(offer);

        // Уведомление помощника
        notificationService.sendNotificationToUser(
            offer.getHelper(),
            "OFFER_REJECTED",
            "Ваше предложение помощи отклонено",
            requestId.toString()
        );

        return offer;
    }

    public AssistanceOffer getUserOfferForRequest(PoputkaUser user, Long requestId) {
        return assistanceOfferRepository.findByRequestIdAndCreatedUser(requestId, user)
            .orElse(null);
    }
}

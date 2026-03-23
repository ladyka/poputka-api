package by.ladyka.poputka.service.roadassistance;

import by.ladyka.poputka.data.dto.roadassistance.AssistanceRequestDto;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceRequest;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceRequestStatus;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceType;
import by.ladyka.poputka.data.repository.roadassistance.AssistanceRequestRepository;
import by.ladyka.poputka.data.repository.roadassistance.AssistanceTypeRepository;
import by.ladyka.poputka.service.NotificationService;
import by.ladyka.poputka.service.mapper.roadassistance.AssistanceRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssistanceRequestService {
    private final AssistanceRequestRepository assistanceRequestRepository;
    private final AssistanceTypeRepository assistanceTypeRepository;
    private final NotificationService notificationService;
    private final UserCompetencyService userCompetencyService;
    private final AssistanceRequestMapper assistanceRequestMapper;

    @Transactional
    public AssistanceRequestDto createRequest(PoputkaUser author, String carInfo, String problemTypeCode,
                                        String description, BigDecimal locationLat, BigDecimal locationLon, 
                                        String address) {
        // Проверка, что пользователь не заблокирован и имеет рейтинг >= 3.0
        if (author.getIsBlocked() != null && author.getIsBlocked()) {
            throw new IllegalStateException("Пользователь заблокирован");
        }
        
        if (author.getRating() != null && author.getRating().compareTo(BigDecimal.valueOf(3.0)) < 0) {
            throw new IllegalStateException("Рейтинг пользователя ниже 3.0");
        }

        // Проверка количества активных запросов пользователя
        List<AssistanceRequestStatus> activeStatuses = Arrays.asList(
            AssistanceRequestStatus.CREATED, 
            AssistanceRequestStatus.OFFER_RECEIVED
        );
        List<AssistanceRequest> activeRequests = assistanceRequestRepository
            .findByCreatedUserAndStatusIn(author, activeStatuses);
        
        if (activeRequests.size() >= 3) {
            throw new IllegalStateException("У пользователя уже есть 3 активных запроса");
        }

        // Получение типа проблемы
        AssistanceType problemType = assistanceTypeRepository.findByCode(problemTypeCode)
            .orElseThrow(() -> new IllegalArgumentException("Тип проблемы не найден"));

        // Создание запроса
        AssistanceRequest request = new AssistanceRequest();
        request.setCarInfo(carInfo);
        request.setProblemType(problemType);
        request.setDescription(description);
        request.setLocationLat(locationLat);
        request.setLocationLon(locationLon);
        request.setAddress(address);
        
        // Установка времени истечения
        Instant now = Instant.now();
        request.setExpiresAt(now.plus(2, ChronoUnit.HOURS));
        request.setMaxExpiresAt(now.plus(48, ChronoUnit.HOURS));
        request.setInitialExpiresAt(request.getExpiresAt());
        request.setLastActivityAt(now);
        
        request = assistanceRequestRepository.save(request);

        // Поиск помощников в радиусе
        findAndNotifyHelpers(author, request);

        return assistanceRequestMapper.toDto(request);
    }

    private void findAndNotifyHelpers(PoputkaUser requester, AssistanceRequest request) {
        // TODO: Реализовать поиск помощников в радиусе с учетом компетенций
        // Пока отправляем уведомление всем готовым помогать
        notificationService.sendNotificationToAllReadyHelpers(
            "NEW_ASSISTANCE_REQUEST", 
            "Новый запрос о помощи рядом", 
            request.getId().toString()
        );
    }

    @Transactional
    public AssistanceRequest extendRequest(PoputkaUser user, Long requestId, int hours) {
        AssistanceRequest request = assistanceRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Запрос не найден"));

        // Проверка прав доступа
        if (!Objects.equals(request.getAuthor().getId(), user.getId())) {
            throw new IllegalStateException("Только автор может продлить запрос");
        }

        // Проверка статуса
        if (!Arrays.asList(AssistanceRequestStatus.CREATED, AssistanceRequestStatus.OFFER_RECEIVED)
                .contains(request.getStatus())) {
            throw new IllegalStateException("Невозможно продлить запрос в текущем статусе");
        }

        // Проверка максимального времени
        Instant newExpiresAt = request.getExpiresAt().plus(hours, ChronoUnit.HOURS);
        if (newExpiresAt.isAfter(request.getMaxExpiresAt())) {
            newExpiresAt = request.getMaxExpiresAt();
        }

        request.setExpiresAt(newExpiresAt);
        request.setLastActivityAt(Instant.now());
        
        request = assistanceRequestRepository.save(request);

        // Уведомить всех участников чата
        notificationService.sendNotificationToChatParticipants(
            request.getId(), 
            "REQUEST_EXPIRING_SOON", 
            "Запрос о помощи продлен"
        );

        return request;
    }

    @Transactional
    public AssistanceRequest cancelRequest(PoputkaUser user, Long requestId) {
        AssistanceRequest request = assistanceRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Запрос не найден"));

        // Проверка прав доступа
        if (!Objects.equals(request.getAuthor().getId(), user.getId())) {
            throw new IllegalStateException("Только автор может отменить запрос");
        }

        request.setStatus(AssistanceRequestStatus.CANCELLED);
        
        return assistanceRequestRepository.save(request);
    }

    public Page<AssistanceRequestDto> findActiveRequestsNearby(BigDecimal lat, BigDecimal lon, 
                                                             BigDecimal radiusKm, Pageable pageable) {
        // TODO: Реализовать поиск запросов в радиусе
        // Пока возвращаем все активные запросы
        List<AssistanceRequestStatus> activeStatuses = Arrays.asList(
            AssistanceRequestStatus.CREATED, 
            AssistanceRequestStatus.OFFER_RECEIVED
        );
        
        // Рассчитываем границы координат для поиска в радиусе
        // Примерный расчет (1 градус широты ≈ 111 км)
        BigDecimal latDelta = radiusKm.divide(BigDecimal.valueOf(111), 8, RoundingMode.HALF_UP);
        BigDecimal lonDelta = radiusKm.divide(BigDecimal.valueOf(111 * Math.cos(Math.toRadians(lat.doubleValue()))), 8, RoundingMode.HALF_UP);
        
        BigDecimal minLat = lat.subtract(latDelta);
        BigDecimal maxLat = lat.add(latDelta);
        BigDecimal minLon = lon.subtract(lonDelta);
        BigDecimal maxLon = lon.add(lonDelta);
        
        Page<AssistanceRequest> requests = assistanceRequestRepository.findByStatusInAndLocationNear(
            activeStatuses, minLat, maxLat, minLon, maxLon, pageable);
        
        return requests.map(assistanceRequestMapper::toDto);
    }

    @Transactional
    public void processExpiredRequests() {
        List<AssistanceRequest> expiredRequests = assistanceRequestRepository
            .findByStatusAndExpiresAtBefore(AssistanceRequestStatus.CREATED, Instant.now());
        
        for (AssistanceRequest request : expiredRequests) {
            request.setStatus(AssistanceRequestStatus.EXPIRED);
            assistanceRequestRepository.save(request);
        }
    }

    public long countActiveRequestsNearby(BigDecimal lat, BigDecimal lon, BigDecimal radiusKm) {
        // TODO: Реализовать подсчет активных запросов в радиусе
        return 0L;
    }

    @Transactional(readOnly = true)
    public AssistanceRequestDto getRequest(Long requestId) {
        AssistanceRequest request = assistanceRequestRepository.findById(requestId).orElse(null);
        return assistanceRequestMapper.toDto(request);
    }
}

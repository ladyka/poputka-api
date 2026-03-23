package by.ladyka.poputka.service.roadassistance;

import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceReview;
import by.ladyka.poputka.data.repository.roadassistance.AssistanceReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRatingService {
    private final AssistanceReviewRepository assistanceReviewRepository;

    @Transactional
    public void recalculateUserRating(PoputkaUser user) {
        // Получаем последние 50 отзывов, отсортированные по дате создания
        Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<AssistanceReview> recentReviews = assistanceReviewRepository
            .findValidReviewsByReviewee(user, pageable).getContent();

        if (recentReviews.isEmpty()) {
            user.setRating(BigDecimal.ZERO);
            return;
        }

        // Рассчитываем средний рейтинг
        BigDecimal sum = recentReviews.stream()
            .map(AssistanceReview::getRating)
            .map(BigDecimal::valueOf)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = sum.divide(BigDecimal.valueOf(recentReviews.size()), 2, RoundingMode.HALF_UP);
        user.setRating(average);

        // Проверяем, нужно ли установить флаг модерации
        if (average.compareTo(BigDecimal.valueOf(3.0)) < 0) {
            user.setRequiresModeration(true);
        } else {
            user.setRequiresModeration(false);
        }
    }

    public boolean isUserBlocked(PoputkaUser user) {
        // Пользователь заблокирован, если его рейтинг ниже 3.0
        return user.getRating() != null && 
               user.getRating().compareTo(BigDecimal.valueOf(3.0)) < 0;
    }
}

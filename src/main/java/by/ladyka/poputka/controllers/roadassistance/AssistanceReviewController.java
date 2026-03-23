package by.ladyka.poputka.controllers.roadassistance;

import by.ladyka.poputka.ApplicationUserDetails;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceReview;
import by.ladyka.poputka.service.roadassistance.AssistanceReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roadassistance")
@RequiredArgsConstructor
public class AssistanceReviewController {
    private final AssistanceReviewService assistanceReviewService;

    @GetMapping("/requests/{requestId}/review")
    public ResponseEntity<AssistanceReview> getReviewDraft(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @PathVariable Long requestId) {
        // TODO: Реализовать получение черновика отзыва
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/requests/{requestId}/review")
    public ResponseEntity<AssistanceReview> createReview(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @PathVariable Long requestId,
            @RequestParam Long revieweeId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment) {
        
        AssistanceReview review = assistanceReviewService.createReview(
            user.user(), requestId, revieweeId, rating, comment);
        
        return ResponseEntity.ok(review);
    }

    @GetMapping("/users/{userId}/reviews")
    public ResponseEntity<Page<AssistanceReview>> getUserReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AssistanceReview> reviews = assistanceReviewService.getUserReviews(null, page, size);
        return ResponseEntity.ok(reviews);
    }

    // Модераторские эндпоинты
    @PostMapping("/reports")
    public ResponseEntity<Void> createReport(
            @AuthenticationPrincipal ApplicationUserDetails user,
            @RequestParam Long reviewId,
            @RequestParam String reason) {
        // TODO: Реализовать создание жалобы на отзыв
        return ResponseEntity.ok().build();
    }

    @GetMapping("/moderation/low-rated-users")
    public ResponseEntity<?> getLowRatedUsersForModeration(
            @AuthenticationPrincipal ApplicationUserDetails moderator) {
        // TODO: Проверка прав модератора
        // TODO: Реализовать получение пользователей с низким рейтингом для модерации
        return ResponseEntity.ok().build();
    }

    @PutMapping("/moderation/reviews/{reviewId}")
    public ResponseEntity<AssistanceReview> moderateReview(
            @AuthenticationPrincipal ApplicationUserDetails moderator,
            @PathVariable Long reviewId,
            @RequestParam boolean isValid,
            @RequestParam(required = false) String moderatorComment) {
        
        // TODO: Проверка прав модератора
        AssistanceReview review = assistanceReviewService.moderateReview(
            moderator.user(), reviewId, isValid, moderatorComment);
        
        return ResponseEntity.ok(review);
    }
}

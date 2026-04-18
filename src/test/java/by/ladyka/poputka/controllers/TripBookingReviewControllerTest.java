package by.ladyka.poputka.controllers;

import by.ladyka.poputka.ApplicationUserDetails;
import by.ladyka.poputka.data.entity.Booking;
import by.ladyka.poputka.data.entity.TripEntity;
import by.ladyka.poputka.data.enums.BookingStatus;
import by.ladyka.poputka.data.repository.BookingRepository;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import by.ladyka.poputka.data.repository.TripRepository;
import by.ladyka.poputka.service.TripBookingReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class TripBookingReviewControllerTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private PoputkaUserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TripBookingReviewService tripBookingReviewService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithUserDetails(value = "test_helper", userDetailsServiceBeanName = "userDetailsService")
    void fullFlow_createDraft_moderateApprove_publicListAndTripRating() throws Exception {
        long tripId = createTripAsOwner("testuser", "REV_FROM", "REV_TO",
                Instant.now().plus(1, ChronoUnit.DAYS), (byte) 3);
        long ownerId = userRepository.findByUsername("testuser").orElseThrow().getId();

        String bookingId = objectMapper.readTree(mockMvc.perform(put("/api/booking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"tripId\": %d}".formatted(tripId)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get("id")
                .asText();

        transitionToCompleted(bookingId);

        mockMvc.perform(post("/api/trip-booking-review/booking/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"comment\":\"Отлично\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.canEdit").value(true))
                .andExpect(jsonPath("$.revieweeId").value((int) ownerId));

        mockMvc.perform(patch("/api/trip-booking-review/{reviewId}", extractReviewId(bookingId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"comment\":\"Спасибо за поездку\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Спасибо за поездку"));

        mockMvc.perform(get("/api/trip-booking-review/booking/{bookingId}/me", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasReview").value(true))
                .andExpect(jsonPath("$.review.status").value("DRAFT"));

        backdateReviewCreatedToHoursAgo(extractReviewId(bookingId), 2);
        assertThat(tripBookingReviewService.processDraftReviewsReadyForModeration()).isGreaterThanOrEqualTo(1);

        mockMvc.perform(get("/api/trip-booking-review/moderation/pending")
                        .with(SecurityMockMvcRequestPostProcessors.user(
                                new ApplicationUserDetails(userRepository.findByUsername("testuser").orElseThrow()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(put("/api/trip-booking-review/moderation/{reviewId}", extractReviewId(bookingId))
                        .with(SecurityMockMvcRequestPostProcessors.user(
                                new ApplicationUserDetails(userRepository.findByUsername("testuser").orElseThrow())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        BigDecimal tripRating = userRepository.findById(ownerId).orElseThrow().getTripRating();
        assertThat(tripRating).isEqualByComparingTo("5.00");

        mockMvc.perform(get("/api/trip-booking-review/users/{userId}", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].rating").value(5))
                .andExpect(jsonPath("$.content[0].tripPlaceFrom").value("REV_FROM"));
    }

    @Test
    @WithUserDetails(value = "test_helper", userDetailsServiceBeanName = "userDetailsService")
    void createReview_whenBookingNotCompleted_returns409() throws Exception {
        long tripId = createTripAsOwner("testuser", "NC_FROM", "NC_TO",
                Instant.now().plus(1, ChronoUnit.DAYS), (byte) 2);
        String bookingId = objectMapper.readTree(mockMvc.perform(put("/api/booking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"tripId\": %d}".formatted(tripId)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get("id")
                .asText();

        mockMvc.perform(post("/api/trip-booking-review/booking/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4}"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithUserDetails(value = "test_lowrating", userDetailsServiceBeanName = "userDetailsService")
    void getMe_whenNotParticipant_returns404() throws Exception {
        long tripId = createTripAsOwner("testuser", "NP_FROM", "NP_TO",
                Instant.now().plus(1, ChronoUnit.DAYS), (byte) 2);
        Booking booking = new Booking();
        booking.setTripId(tripId);
        booking.setPassengerId(userRepository.findByUsername("test_helper").orElseThrow().getId());
        booking.setBookingStatus(BookingStatus.COMPLETED);
        String bookingId = bookingRepository.save(booking).getId();

        mockMvc.perform(get("/api/trip-booking-review/booking/{bookingId}/me", bookingId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails(value = "test_helper", userDetailsServiceBeanName = "userDetailsService")
    void moderation_whenNotModerator_returns403() throws Exception {
        mockMvc.perform(get("/api/trip-booking-review/moderation/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "test_helper", userDetailsServiceBeanName = "userDetailsService")
    void patchReview_emptyBody_returns422() throws Exception {
        long tripId = createTripAsOwner("testuser", "PB_FROM", "PB_TO",
                Instant.now().plus(1, ChronoUnit.DAYS), (byte) 2);
        String bookingId = objectMapper.readTree(mockMvc.perform(put("/api/booking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"tripId\": %d}".formatted(tripId)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get("id")
                .asText();
        transitionToCompleted(bookingId);

        mockMvc.perform(post("/api/trip-booking-review/booking/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":3}"))
                .andExpect(status().isOk());
        long reviewId = extractReviewId(bookingId);

        mockMvc.perform(patch("/api/trip-booking-review/{reviewId}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails(value = "test_helper", userDetailsServiceBeanName = "userDetailsService")
    void createReview_duplicate_returns409() throws Exception {
        long tripId = createTripAsOwner("testuser", "DP_FROM", "DP_TO",
                Instant.now().plus(1, ChronoUnit.DAYS), (byte) 2);
        String bookingId = objectMapper.readTree(mockMvc.perform(put("/api/booking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"tripId\": %d}".formatted(tripId)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get("id")
                .asText();
        transitionToCompleted(bookingId);

        mockMvc.perform(post("/api/trip-booking-review/booking/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/trip-booking-review/booking/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4}"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithUserDetails(value = "test_helper", userDetailsServiceBeanName = "userDetailsService")
    void afterRejected_secondCreate_returns409() throws Exception {
        long tripId = createTripAsOwner("testuser", "RJ_FROM", "RJ_TO",
                Instant.now().plus(1, ChronoUnit.DAYS), (byte) 2);
        String bookingId = objectMapper.readTree(mockMvc.perform(put("/api/booking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"tripId\": %d}".formatted(tripId)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get("id")
                .asText();
        transitionToCompleted(bookingId);

        mockMvc.perform(post("/api/trip-booking-review/booking/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":2,\"comment\":\"плохо\"}"))
                .andExpect(status().isOk());
        long reviewId = extractReviewId(bookingId);

        backdateReviewCreatedToHoursAgo(reviewId, 2);
        tripBookingReviewService.processDraftReviewsReadyForModeration();

        mockMvc.perform(put("/api/trip-booking-review/moderation/{reviewId}", reviewId)
                        .with(SecurityMockMvcRequestPostProcessors.user(
                                new ApplicationUserDetails(userRepository.findByUsername("testuser").orElseThrow())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"REJECT\",\"moderatorComment\":\"no\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mockMvc.perform(post("/api/trip-booking-review/booking/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5}"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithUserDetails(value = "test_helper", userDetailsServiceBeanName = "userDetailsService")
    void patchReview_afterModerationSubmitted_returns409() throws Exception {
        long tripId = createTripAsOwner("testuser", "PM_FROM", "PM_TO",
                Instant.now().plus(1, ChronoUnit.DAYS), (byte) 2);
        String bookingId = objectMapper.readTree(mockMvc.perform(put("/api/booking")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"tripId\": %d}".formatted(tripId)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .get("id")
                .asText();
        transitionToCompleted(bookingId);

        mockMvc.perform(post("/api/trip-booking-review/booking/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4}"))
                .andExpect(status().isOk());
        long reviewId = extractReviewId(bookingId);

        backdateReviewCreatedToHoursAgo(reviewId, 2);
        tripBookingReviewService.processDraftReviewsReadyForModeration();

        mockMvc.perform(patch("/api/trip-booking-review/{reviewId}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5}"))
                .andExpect(status().isConflict());
    }

    private long extractReviewId(String bookingId) throws Exception {
        String json = mockMvc.perform(get("/api/trip-booking-review/booking/{bookingId}/me", bookingId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(json).get("review").get("id").asLong();
    }

    private void transitionToCompleted(String bookingId) throws Exception {
        mockMvc.perform(post("/api/booking/{bookingId}/status", bookingId)
                        .with(SecurityMockMvcRequestPostProcessors.user(
                                new ApplicationUserDetails(userRepository.findByUsername("testuser").orElseThrow())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"ACCEPTED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/booking/{bookingId}/status", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"CHECKED_IN\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/booking/{bookingId}/status", bookingId)
                        .with(SecurityMockMvcRequestPostProcessors.user(
                                new ApplicationUserDetails(userRepository.findByUsername("testuser").orElseThrow())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/booking/{bookingId}/status", bookingId)
                        .with(SecurityMockMvcRequestPostProcessors.user(
                                new ApplicationUserDetails(userRepository.findByUsername("testuser").orElseThrow())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"COMPLETED\"}"))
                .andExpect(status().isOk());
    }

    private void backdateReviewCreatedToHoursAgo(long reviewId, long hours) {
        long millis = Instant.now().minus(hours, ChronoUnit.HOURS).toEpochMilli();
        jdbcTemplate.update("UPDATE trip_booking_review SET created_datetime = ? WHERE id = ?", millis, reviewId);
    }

    private long createTripAsOwner(String ownerUsername, String from, String to, Instant start, byte passengers) {
        long ownerId = userRepository.findByUsername(ownerUsername).orElseThrow().getId();
        TripEntity trip = new TripEntity();
        trip.setOwnerId(ownerId);
        trip.setPlaceFrom(from);
        trip.setPlaceTo(to);
        trip.setStartTime(start);
        trip.setPassengers(passengers);
        trip.setDescription("trip for review test");
        return tripRepository.save(trip).getId();
    }
}

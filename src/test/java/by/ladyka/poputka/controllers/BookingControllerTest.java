package by.ladyka.poputka.controllers;

import by.ladyka.poputka.ApplicationUserDetails;
import by.ladyka.poputka.data.entity.Booking;
import by.ladyka.poputka.data.entity.BookingMessage;
import by.ladyka.poputka.data.entity.TripEntity;
import by.ladyka.poputka.data.enums.BookingStatus;
import by.ladyka.poputka.data.repository.BookingMessageRepository;
import by.ladyka.poputka.data.repository.BookingRepository;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import by.ladyka.poputka.data.repository.TripRepository;
import by.ladyka.poputka.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for booking chat payloads, status transitions, and related services.
 */
@Transactional
class BookingControllerTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private PoputkaUserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingMessageRepository bookingMessageRepository;

    @Autowired
    private BookingService bookingService;

    @Test
    @WithUserDetails(value = "test_helper", userDetailsServiceBeanName = "userDetailsService")
    void bookingFlow_messagesPayloadAndStatusTransitions() throws Exception {
        long tripId = createTripEntityAsOwner("testuser", "BOOK_FROM", "BOOK_TO",
                Instant.now().plus(2, ChronoUnit.DAYS), (byte) 3);

        String createBody = """
                {"tripId": %d, "message": "Хочу место"}
                """.formatted(tripId);

        String bookingResponse = mockMvc.perform(put("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("WAITING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String bookingId = objectMapper.readTree(bookingResponse).get("id").asText();

        mockMvc.perform(get("/api/booking/{bookingId}/available-statuses", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", containsInAnyOrder("CANCELLED")));

        assertThatThrownBy(() -> mockMvc.perform(post("/api/booking/{bookingId}/status", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"ACCEPTED\"}"))
                .andReturn())
                .isInstanceOf(ServletException.class)
                .hasRootCauseInstanceOf(RuntimeException.class);

        mockMvc.perform(get("/api/booking/messages/{bookingId}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].payload.type").value("MESSAGE"))
                .andExpect(jsonPath("$[0].payload.text").value("Хочу место"));

        mockMvc.perform(get("/api/booking/{bookingId}/available-statuses", bookingId)
                        .with(SecurityMockMvcRequestPostProcessors.user(
                                new ApplicationUserDetails(
                                        userRepository.findByUsername("testuser").orElseThrow()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", containsInAnyOrder("ACCEPTED", "REJECTED", "CANCELLED")));

        mockMvc.perform(post("/api/booking/{bookingId}/status", bookingId)
                        .with(SecurityMockMvcRequestPostProcessors.user(
                                new ApplicationUserDetails(
                                        userRepository.findByUsername("testuser").orElseThrow())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"ACCEPTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("ACCEPTED"));

        mockMvc.perform(get("/api/booking/messages/{bookingId}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].payload.type").value("SERVICE"))
                .andExpect(jsonPath("$[1].payload.event").value("BOOKING_STATUS_CHANGED"))
                .andExpect(jsonPath("$[1].payload.from").value("WAITING"))
                .andExpect(jsonPath("$[1].payload.to").value("ACCEPTED"));

        mockMvc.perform(post("/api/booking/{bookingId}/status", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"CHECKED_IN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("CHECKED_IN"));

        mockMvc.perform(post("/api/booking/{bookingId}/status", bookingId)
                        .with(SecurityMockMvcRequestPostProcessors.user(
                                new ApplicationUserDetails(
                                        userRepository.findByUsername("testuser").orElseThrow())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("IN_PROGRESS"));

        mockMvc.perform(post("/api/booking/{bookingId}/status", bookingId)
                        .with(SecurityMockMvcRequestPostProcessors.user(
                                new ApplicationUserDetails(
                                        userRepository.findByUsername("testuser").orElseThrow())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("COMPLETED"));

        mockMvc.perform(put("/api/booking/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookingId": "%s",
                                  "payload": {
                                    "type": "MESSAGE",
                                    "text": "Спасибо за поездку"
                                  }
                                }
                                """.formatted(bookingId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.type").value("MESSAGE"))
                .andExpect(jsonPath("$.payload.text").value("Спасибо за поездку"));
    }

    @Test
    @WithUserDetails(value = "test_helper", userDetailsServiceBeanName = "userDetailsService")
    void sendMessage_withContentOnly_buildsMessagePayload() throws Exception {
        long tripId = createTripEntityAsOwner("testuser", "C_FROM", "C_TO",
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

        mockMvc.perform(put("/api/booking/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookingId\":\"%s\",\"content\":\"plain\"}".formatted(bookingId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.type").value("MESSAGE"))
                .andExpect(jsonPath("$.payload.text").value("plain"));
    }

    @Test
    @WithUserDetails(value = "test_helper", userDetailsServiceBeanName = "userDetailsService")
    void systemExpireWaitingBooking_setsExpiredAndServiceMessage() {
        long ownerId = userRepository.findByUsername("testuser").orElseThrow().getId();
        long passengerId = userRepository.findByUsername("test_helper").orElseThrow().getId();

        TripEntity trip = new TripEntity();
        trip.setOwnerId(ownerId);
        trip.setPlaceFrom("EXP_FROM");
        trip.setPlaceTo("EXP_TO");
        trip.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        trip.setPassengers((byte) 2);
        trip.setDescription("exp");
        long tripId = tripRepository.save(trip).getId();

        Booking booking = new Booking();
        booking.setTripId(tripId);
        booking.setPassengerId(passengerId);
        booking.setBookingStatus(BookingStatus.WAITING);
        String bookingId = bookingRepository.save(booking).getId();

        bookingService.systemExpireWaitingBooking(bookingId);

        Booking reloaded = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(reloaded.getBookingStatus()).isEqualTo(BookingStatus.EXPIRED);

        List<BookingMessage> messages = bookingMessageRepository.findByBookingId(bookingId);
        assertThat(messages).anySatisfy(m -> {
            var p = m.getPayload();
            assertThat(p).isInstanceOf(by.ladyka.poputka.data.dto.payload.MessagePayload.Service.class);
            var service = (by.ladyka.poputka.data.dto.payload.MessagePayload.Service) p;
            assertThat(service.event()).isEqualTo("BOOKING_STATUS_CHANGED");
            assertThat(service.from()).isEqualTo("WAITING");
            assertThat(service.to()).isEqualTo("EXPIRED");
            assertThat(m.getSenderId()).isEqualTo(ownerId);
        });
    }

    @Test
    @WithUserDetails(value = "test_lowrating", userDetailsServiceBeanName = "userDetailsService")
    void messages_forbiddenForNonParticipant() throws Exception {
        long tripId = createTripEntityAsOwner("testuser", "NF_FROM", "NF_TO",
                Instant.now().plus(1, ChronoUnit.DAYS), (byte) 2);
        long passengerId = userRepository.findByUsername("test_helper").orElseThrow().getId();
        Booking booking = new Booking();
        booking.setTripId(tripId);
        booking.setPassengerId(passengerId);
        booking.setBookingStatus(BookingStatus.WAITING);
        String bookingId = bookingRepository.save(booking).getId();

        assertThatThrownBy(() -> mockMvc.perform(get("/api/booking/messages/{bookingId}", bookingId))
                .andReturn())
                .isInstanceOf(ServletException.class)
                .hasRootCauseInstanceOf(RuntimeException.class);
    }

    private long createTripEntityAsOwner(String ownerUsername, String from, String to,
                                         Instant start, byte passengers) {
        long ownerId = userRepository.findByUsername(ownerUsername).orElseThrow().getId();
        TripEntity entity = new TripEntity();
        entity.setOwnerId(ownerId);
        entity.setPlaceFrom(from);
        entity.setPlaceTo(to);
        entity.setStartTime(start);
        entity.setPassengers(passengers);
        entity.setDescription("test trip");
        return tripRepository.save(entity).getId();
    }
}

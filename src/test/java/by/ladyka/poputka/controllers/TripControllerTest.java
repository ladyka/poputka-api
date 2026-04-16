package by.ladyka.poputka.controllers;

import by.ladyka.poputka.data.repository.TripRepository;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import by.ladyka.poputka.data.repository.BookingRepository;
import by.ladyka.poputka.data.entity.TripEntity;
import by.ladyka.poputka.data.entity.Booking;
import by.ladyka.poputka.data.enums.BookingStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the {@link TripController}
 */
@Transactional
class TripControllerTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private PoputkaUserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void create_shouldReturnOkAndGeneratedId_whenAuthenticated() throws Exception {
        String dto = tripDtoJson(-1, "A", 100, "relation", "B", 200, "relation",
                Instant.now().plus(2, ChronoUnit.DAYS), (byte) 3, "desc");

        mockMvc.perform(post("/api/trip/")
                        .content(dto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.from").value("A"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.to").value("B"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.passengers").value(3));
    }

    @Test
    void create_shouldReject_whenUnauthenticated() throws Exception {
        String dto = tripDtoJson(-1, "A", 100, "relation", "B", 200, "relation",
                Instant.now().plus(2, ChronoUnit.DAYS), (byte) 1, "");

        mockMvc.perform(post("/api/trip/")
                        .content(dto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void update_shouldUpdateExistingTrip_whenOwner() throws Exception {
        long id = createTripAndGetId("Minsk", "Grodno", Instant.now().plus(3, ChronoUnit.DAYS), (byte) 2, "v1");

        String updateDto = tripDtoJson(id, "Minsk", 1, "relation", "Brest", 2, "relation",
                Instant.now().plus(4, ChronoUnit.DAYS), (byte) 3, "v2");

        mockMvc.perform(post("/api/trip/")
                        .content(updateDto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.to").value("Brest"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.passengers").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("v2"));
    }

    @Test
    @WithUserDetails(value = "test_helper", userDetailsServiceBeanName = "userDetailsService")
    void update_shouldReturnNotFound_whenUpdatingForeignTrip() throws Exception {
        long tripId = createTripAndGetIdAsUser("testuser", "FOREIGN_FROM", "FOREIGN_TO",
                Instant.now().plus(3, ChronoUnit.DAYS), (byte) 1, "foreign");

        String updateDto = tripDtoJson(tripId, "FOREIGN_FROM", 1, "relation", "NEW_TO", 2, "relation",
                Instant.now().plus(4, ChronoUnit.DAYS), (byte) 2, "attempt");

        mockMvc.perform(post("/api/trip/")
                        .content(updateDto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void search_shouldReturnEmptyList_whenNoTrips() throws Exception {
        tripRepository.deleteAll();

        mockMvc.perform(post("/api/trip/search")
                        .content("""
                                {"placeFrom":"A","placeTo":"B"}
                                """)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(0)));
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void search_shouldReturnOnlyFutureTripsSortedByStart() throws Exception {
        // Past (should be filtered out)
        createTripAndGetId("F", "T", Instant.now().minus(2, ChronoUnit.HOURS), (byte) 1, "past");
        // Future
        long id1 = createTripAndGetId("F", "T", Instant.now().plus(2, ChronoUnit.HOURS), (byte) 1, "f1");
        long id2 = createTripAndGetId("F", "T", Instant.now().plus(3, ChronoUnit.HOURS), (byte) 1, "f2");

        mockMvc.perform(post("/api/trip/search")
                        .content("""
                                {"placeFrom":"F","placeTo":"T"}
                                """)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(id1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(id2));
    }

    @Test
    void getById_shouldNotFail_whenTripDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/trip/{id}", 999_999L))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void popular_shouldReturnRoutesSortedByCountDesc() throws Exception {
        // A->B x2
        createTripAndGetId("A", "B", Instant.now().plus(1, ChronoUnit.DAYS), (byte) 1, "r1");
        createTripAndGetId("A", "B", Instant.now().plus(2, ChronoUnit.DAYS), (byte) 1, "r2");
        // C->D x1
        createTripAndGetId("C", "D", Instant.now().plus(3, ChronoUnit.DAYS), (byte) 1, "r3");

        mockMvc.perform(get("/api/trip/popular"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].placeFrom").value("A"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].placeTo").value("B"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].c").value(2));
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void popular_shouldIgnorePastTrips() throws Exception {
        // Same route: one past, one future. Past must be ignored.
        createTripAndGetId("PAST_A", "PAST_B", Instant.now().minus(2, ChronoUnit.DAYS), (byte) 1, "past");
        createTripAndGetId("PAST_A", "PAST_B", Instant.now().plus(2, ChronoUnit.DAYS), (byte) 1, "future");

        mockMvc.perform(get("/api/trip/popular"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].placeFrom").value("PAST_A"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].placeTo").value("PAST_B"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].c").value(1));
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void oldTrip_shouldBeVisibleForOwner() throws Exception {
        long oldTripId = createTripEntityAndGetId("testuser", "OLD_FROM", "OLD_TO",
                Instant.now().minus(20, ChronoUnit.DAYS), (byte) 1, "old");

        mockMvc.perform(get("/api/trip/{id}", oldTripId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(oldTripId));
    }

    @Test
    @WithUserDetails(value = "test_helper", userDetailsServiceBeanName = "userDetailsService")
    void oldTrip_shouldBeVisibleForPassengerWhoBooked() throws Exception {
        long oldTripId = createTripEntityAndGetId("testuser", "OLD2_FROM", "OLD2_TO",
                Instant.now().minus(20, ChronoUnit.DAYS), (byte) 2, "old2");

        long passengerId = userRepository.findByUsername("test_helper").orElseThrow().getId();
        Booking booking = new Booking();
        booking.setTripId(oldTripId);
        booking.setPassengerId(passengerId);
        booking.setBookingStatus(BookingStatus.ACCEPTED);
        bookingRepository.save(booking);

        mockMvc.perform(get("/api/trip/{id}", oldTripId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(oldTripId));
    }

    @Test
    @WithUserDetails(value = "test_lowrating", userDetailsServiceBeanName = "userDetailsService")
    void oldTrip_shouldNotBeVisibleForRandomUser() throws Exception {
        long oldTripId = createTripEntityAndGetId("testuser", "OLD3_FROM", "OLD3_TO",
                Instant.now().minus(20, ChronoUnit.DAYS), (byte) 1, "old3");

        mockMvc.perform(get("/api/trip/{id}", oldTripId))
                .andExpect(status().isNotFound());
    }

    @Test
    void oldTrip_shouldNotBeVisibleForAnonymous() throws Exception {
        long oldTripId = createTripAndGetIdAsUser("testuser", "OLD4_FROM", "OLD4_TO",
                Instant.now().minus(20, ChronoUnit.DAYS), (byte) 1, "old4");

        mockMvc.perform(get("/api/trip/{id}", oldTripId))
                .andExpect(status().isNotFound());
    }

    private long createTripAndGetIdAsUser(String username, String from, String to, Instant start, byte passengers, String description) throws Exception {
        String dto = tripDtoJson(-1, from, 10, "relation", to, 20, "relation", start, passengers, description);
        String response = mockMvc.perform(post("/api/trip/")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(
                                userDetailsService.loadUserByUsername(username)))
                        .content(dto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> node = objectMapper.readValue(response, new TypeReference<>() {});
        return ((Number) node.get("id")).longValue();
    }

    private long createTripEntityAndGetId(String ownerUsername, String from, String to, Instant start, byte passengers, String description) {
        long ownerId = userRepository.findByUsername(ownerUsername)
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElseThrow())
                .getId();
        TripEntity entity = new TripEntity();
        entity.setOwnerId(ownerId);
        entity.setPlaceFrom(from);
        entity.setPlaceTo(to);
        entity.setStartTime(start);
        entity.setPassengers(passengers);
        entity.setDescription(description);
        return tripRepository.save(entity).getId();
    }

    private long createTripAndGetId(String from, String to, Instant start, byte passengers, String description) throws Exception {
        String dto = tripDtoJson(-1, from, 10, "relation", to, 20, "relation", start, passengers, description);
        String response = mockMvc.perform(post("/api/trip/")
                        .content(dto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> node = objectMapper.readValue(response, new TypeReference<>() {});
        return ((Number) node.get("id")).longValue();
    }

    private static String tripDtoJson(long id,
                                      String fromName, long fromOsmId, String fromOsmType,
                                      String toName, long toOsmId, String toOsmType,
                                      Instant start,
                                      byte passengers,
                                      String description) {
        return """
                {
                  "start": "%s",
                  "description": "%s",
                  "from": {
                    "name": "%s",
                    "osm_id": %d,
                    "osm_type": "%s"
                  },
                  "id": %d,
                  "to": {
                    "name": "%s",
                    "osm_id": %d,
                    "osm_type": "%s"
                  },
                  "passengers": %d
                }""".formatted(start.toString(), description, fromName, fromOsmId, fromOsmType, id, toName, toOsmId, toOsmType, passengers);
    }
}

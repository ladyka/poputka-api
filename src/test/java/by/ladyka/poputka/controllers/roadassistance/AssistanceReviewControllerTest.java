package by.ladyka.poputka.controllers.roadassistance;

import by.ladyka.poputka.controllers.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

class AssistanceReviewControllerTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    // @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void getReviewDraft_shouldReturnNotFound_forNow() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/roadassistance/requests/{requestId}/review", 999_999L))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    // @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void createReview_shouldCreateReview() throws Exception {
        Long requestId = createRequestAsAuthorAndGetId();

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/roadassistance/requests/{requestId}/review", requestId)
                                .param("revieweeId", "2")
                                .param("rating", "5")
                                .param("comment", "Great helper"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    // @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void getUserReviews_shouldReturnPage() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/roadassistance/users/{userId}/reviews", 2L)
                                .param("page", "0")
                                .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    private Long createRequestAsAuthorAndGetId() throws Exception {
        String response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/roadassistance/requests")
                                .param("carInfo", "Review test car")
                                .param("problemTypeCode", "TOWING")
                                .param("description", "Review test request")
                                .param("locationLat", "53.90000000")
                                .param("locationLon", "27.56670000")
                                .param("address", "Minsk"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        return node.get("id").asLong();
    }
}


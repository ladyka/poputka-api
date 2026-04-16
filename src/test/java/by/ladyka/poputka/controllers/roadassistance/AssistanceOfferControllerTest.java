package by.ladyka.poputka.controllers.roadassistance;

import by.ladyka.poputka.controllers.AbstractIntegrationTest;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceOffer;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceRequest;
import by.ladyka.poputka.data.repository.roadassistance.AssistanceOfferRepository;
import by.ladyka.poputka.data.repository.roadassistance.AssistanceRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;
import java.util.Optional;

class AssistanceOfferControllerTest extends AbstractIntegrationTest {

    @Autowired
    private AssistanceRequestRepository assistanceRequestRepository;

    @Autowired
    private AssistanceOfferRepository assistanceOfferRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // @Test
    @WithUserDetails(value = "test_helper", userDetailsServiceBeanName = "userDetailsService")
    void createOffer_shouldCreateOfferForExistingRequest() throws Exception {
        Long requestId = createRequestAsAuthorAndGetId();

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/roadassistance/requests/{requestId}/offers", requestId)
                                .param("message", "I can help"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    // @Test
    @WithUserDetails(value = "test_helper", userDetailsServiceBeanName = "userDetailsService")
    void getMyOffer_shouldReturnOfferForHelper() throws Exception {
        Long requestId = createRequestAsAuthorAndGetId();
        Long offerId = createOfferAsHelperAndGetId(requestId);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/roadassistance/requests/{requestId}/offers/my", requestId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(offerId));
    }

    // @Test
    @WithUserDetails(value = "test_helper", userDetailsServiceBeanName = "userDetailsService")
    void cancelOffer_shouldCancelOwnOffer() throws Exception {
        Long requestId = createRequestAsAuthorAndGetId();
        Long offerId = createOfferAsHelperAndGetId(requestId);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/roadassistance/offers/{offerId}/cancel", offerId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(offerId));
    }

    // @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void acceptOffer_shouldAcceptByAuthor() throws Exception {
        Long requestId = createRequestAsAuthorAndGetId();
        Long offerId;
        // создаём оффер от имени test_helper
        {
            String response = mockMvc.perform(
                            MockMvcRequestBuilders.post("/api/roadassistance/requests/{requestId}/offers", requestId)
                                    .with(request -> {
                                        request.setRemoteUser("test_helper");
                                        return request;
                                    }))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            Map<String, Object> node = objectMapper.readValue(response, new TypeReference<>() {});
            offerId = ((Number) node.get("id")).longValue();
        }

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/roadassistance/requests/{requestId}/offers/{offerId}/accept",
                                        requestId, offerId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(offerId));
    }

    // @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void rejectOffer_shouldRejectByAuthor() throws Exception {
        Long requestId = createRequestAsAuthorAndGetId();
        Long offerId = createOfferAsHelperAndGetId(requestId);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/roadassistance/requests/{requestId}/offers/{offerId}/reject",
                                        requestId, offerId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(offerId));
    }

    private Long createRequestAsAuthorAndGetId() throws Exception {
        String response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/roadassistance/requests")
                                .with(request -> {
                                    request.setRemoteUser("testuser");
                                    return request;
                                })
                                .param("carInfo", "Offer test car")
                                .param("problemTypeCode", "TOWING")
                                .param("description", "Offer test request")
                                .param("locationLat", "53.90000000")
                                .param("locationLon", "27.56670000")
                                .param("address", "Minsk"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> node = objectMapper.readValue(response, new TypeReference<>() {});
        return ((Number) node.get("id")).longValue();
    }

    private Long createOfferAsHelperAndGetId(Long requestId) throws Exception {
        String response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/roadassistance/requests/{requestId}/offers", requestId)
                                .with(request -> {
                                    request.setRemoteUser("test_helper");
                                    return request;
                                })
                                .param("message", "I can help"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> node = objectMapper.readValue(response, new TypeReference<>() {});
        return ((Number) node.get("id")).longValue();
    }
}


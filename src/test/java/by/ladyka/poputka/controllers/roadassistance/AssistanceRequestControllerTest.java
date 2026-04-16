package by.ladyka.poputka.controllers.roadassistance;

import by.ladyka.poputka.controllers.AbstractIntegrationTest;
import by.ladyka.poputka.data.entity.roadassistance.AssistanceRequest;
import by.ladyka.poputka.data.repository.roadassistance.AssistanceRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Transactional
class AssistanceRequestControllerTest extends AbstractIntegrationTest {

    @Autowired
    private AssistanceRequestRepository assistanceRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void searchRequests_shouldReturnPageOfActiveRequests() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/roadassistance/requests")
                        .param("lat", "53.90000000")
                        .param("lon", "27.56670000")
                        .param("radiusKm", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageable").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.number").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.sort").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.first").isBoolean())
                .andExpect(MockMvcResultMatchers.jsonPath("$.last").isBoolean())
                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").isNumber());
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void searchRequests_shouldReturnDtoFields_whenRequestsExist() throws Exception {
        // Create test requests first
        createRequestAndGetId();
        createRequestAndGetId();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/roadassistance/requests")
                        .param("lat", "53.90000000")
                        .param("lon", "27.56670000")
                        .param("radiusKm", "10")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].authorId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].authorName").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].carInfo").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].problemTypeId").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].problemTypeName").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].description").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].locationLat").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].locationLon").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].address").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].status").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].createdDatetime").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].modifiedDatetime").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].expiresAt").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].initialExpiresAt").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].maxExpiresAt").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].lastActivityAt").exists());
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void searchRequests_shouldReturnEmptyPage_whenNoRequestsMatch() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/roadassistance/requests")
                        .param("lat", "0.00000000")
                        .param("lon", "0.00000000")
                        .param("radiusKm", "1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(0));
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void searchRequests_shouldHandlePagination_whenPageAndSizeProvided() throws Exception {
        // Create 3 requests to test pagination (respects the 3-request limit)
        for (int i = 0; i < 3; i++) {
            createRequestAndGetId();
        }

        mockMvc.perform(MockMvcRequestBuilders.get("/api/roadassistance/requests")
                        .param("lat", "53.90000000")
                        .param("lon", "27.56670000")
                        .param("radiusKm", "10")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.number").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").isNumber());
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void getRequest_shouldReturnNotFound_whenRequestDoesNotExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/roadassistance/requests/{id}", 999_999L))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void getRequest_shouldReturnExistingRequest() throws Exception {
        Long requestId = createRequestAndGetId();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/roadassistance/requests/{id}", requestId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(requestId));
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void createRequest_shouldCreateNewRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/roadassistance/requests")
                        .param("carInfo", "Test car")
                        .param("problemTypeCode", "TOWING")
                        .param("description", "Engine problem")
                        .param("locationLat", "53.00000000")
                        .param("locationLon", "27.00000000")
                        .param("address", "Place test"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void extendRequest_shouldExtendForAuthor() throws Exception {
        Long requestId = createRequestAndGetId();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/roadassistance/requests/{id}/extend", requestId)
                        .param("hours", "1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(requestId));
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void cancelRequest_shouldCancelForAuthor() throws Exception {
        Long requestId = createRequestAndGetId();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/roadassistance/requests/{id}/cancel", requestId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(requestId));
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void getActiveRequestsCount_shouldReturnZeroForNow() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/roadassistance/requests/active/count")
                        .param("lat", "53.90000000")
                        .param("lon", "27.56670000")
                        .param("radiusKm", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value(0));
    }

    private Optional<AssistanceRequest> findSeedRequest() {
        return assistanceRequestRepository.findAll().stream().findFirst();
    }

    private Long createRequestAndGetId() throws Exception {
        return createRequestAndGetId("Seed car", "TOWING", "Seed request");
    }

    private Long createRequestAndGetId(String carInfo, String problemTypeCode, String description) throws Exception {
        String response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/roadassistance/requests")
                                .param("carInfo", carInfo)
                                .param("problemTypeCode", problemTypeCode)
                                .param("description", description)
                                .param("locationLat", "53.90000000")
                                .param("locationLon", "27.56670000")
                                .param("address", "Minsk seed"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> node = objectMapper.readValue(response, new TypeReference<>() {});
        return ((Number) node.get("id")).longValue();
    }
}


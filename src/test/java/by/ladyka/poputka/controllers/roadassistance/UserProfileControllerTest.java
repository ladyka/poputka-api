package by.ladyka.poputka.controllers.roadassistance;

import by.ladyka.poputka.controllers.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

class UserProfileControllerTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    // @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void updateProfile_shouldUpdateHelperSettings() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/roadassistance/profile")
                                .param("readyToHelp", "true")
                                .param("helpRadius", "15"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("testuser"));
    }

    // @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void getAllCompetencies_shouldReturnList() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/roadassistance/competencies"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray());
    }

    // @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void getUserCompetencies_shouldReturnUserList() throws Exception {
        Long competencyId = getFirstCompetencyId();

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/roadassistance/profile")
                                .param("readyToHelp", "true")
                                .param("helpRadius", "10")
                                .param("competencyIds", competencyId.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/roadassistance/profile/competencies"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray());
    }

    private Long getFirstCompetencyId() throws Exception {
        String response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/roadassistance/competencies"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        return node.get(0).get("id").asLong();
    }
}


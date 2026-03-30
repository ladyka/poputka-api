package by.ladyka.poputka.controllers;

import by.ladyka.poputka.WebSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the {@link TripController}
 */
@Transactional
public class TripControllerTest  extends AbstractIntegrationTest {

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    public void update() throws Exception {
        String dto = """
                     {
                       "start": "2026-03-30T13:00:00.320Z",
                       "description": "",
                       "from": {
                         "name": "A",
                         "osm_id": 100,
                         "osm_type": "relation"
                       },
                       "id": -1,
                       "to": {
                         "name": "B",
                         "osm_id": 200,
                         "osm_type": "relation"
                       },
                       "passengers": 3
                     }""";

        mockMvc.perform(post("/api/trip/")
                        .content(dto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());;
    }
}

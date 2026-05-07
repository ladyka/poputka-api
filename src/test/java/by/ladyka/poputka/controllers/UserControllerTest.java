package by.ladyka.poputka.controllers;

import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class UserControllerTest extends AbstractIntegrationTest {

    @Autowired
    private PoputkaUserRepository userRepository;

    @Test
    void signup_shouldTrimInvisibleWhitespaceInEmail() throws Exception {
        mockMvc.perform(post("/api/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": " \\u00A0  user@example.com  ",
                                  "password": "pass",
                                  "name": "N",
                                  "surname": "S"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(userRepository.findByUsername("user@example.com")).isPresent();
        assertThat(userRepository.findByUsername(" \u00A0  user@example.com  ")).isNotPresent();
    }
}


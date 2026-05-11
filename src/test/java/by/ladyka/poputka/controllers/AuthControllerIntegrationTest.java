package by.ladyka.poputka.controllers;

import by.ladyka.poputka.auth.AppleTokenVerificationService;
import by.ladyka.poputka.auth.AppleUserPrincipal;
import by.ladyka.poputka.auth.GoogleTokenVerificationService;
import by.ladyka.poputka.auth.GoogleUserPrincipal;
import by.ladyka.poputka.auth.JwtBearerConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(AuthControllerIntegrationTest.OAuthVerifierStubConfig.class)
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private GoogleTokenVerificationService googleTokenVerificationService;

    @Autowired
    private AppleTokenVerificationService appleTokenVerificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void resetOAuthMocks() {
        Mockito.reset(googleTokenVerificationService);
        Mockito.reset(appleTokenVerificationService);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class OAuthVerifierStubConfig {
        @Bean
        @Primary
        GoogleTokenVerificationService googleTokenVerificationService() {
            return Mockito.mock(GoogleTokenVerificationService.class);
        }

        @Bean
        @Primary
        AppleTokenVerificationService appleTokenVerificationService() {
            return Mockito.mock(AppleTokenVerificationService.class);
        }
    }

    @Test
    void tripOwned_withoutBearer_returns401() throws Exception {
        mockMvc.perform(get("/api/trip/owned"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passwordLogin_thenProtectedEndpoint_returns200() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"testuser","password":"password"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(login.getResponse().getContentAsString());
        String access = json.path("accessToken").asText();
        assertThat(access).isNotBlank();

        mockMvc.perform(get("/api/trip/owned")
                        .header(JwtBearerConstants.AUTHORIZATION_HEADER,
                                JwtBearerConstants.BEARER_PREFIX + access))
                .andExpect(status().isOk());
    }

    @Test
    void googleAuth_createsUserAndReturnsTokens() throws Exception {
        String email = "google-" + UUID.randomUUID() + "@example.test";
        when(googleTokenVerificationService.verify(anyString()))
                .thenReturn(new GoogleUserPrincipal("sub-" + UUID.randomUUID(), email, true));
        MvcResult res = mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"fake-id-token\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        assertThat(json.path("refreshToken").asText()).isNotBlank();
        String access = json.path("accessToken").asText();
        mockMvc.perform(get("/api/trip/owned")
                        .header(JwtBearerConstants.AUTHORIZATION_HEADER,
                                JwtBearerConstants.BEARER_PREFIX + access))
                .andExpect(status().isOk());
    }

    @Test
    void appleAuth_createsUserAndReturnsTokens() throws Exception {
        String email = "apple-" + UUID.randomUUID() + "@example.test";
        when(appleTokenVerificationService.verify(anyString()))
                .thenReturn(new AppleUserPrincipal("apple-sub-" + UUID.randomUUID(), email, true));
        MvcResult res = mockMvc.perform(post("/api/auth/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identityToken\":\"fake-identity-token\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        assertThat(json.path("refreshToken").asText()).isNotBlank();
        assertThat(json.path("user").path("appleLinked").asBoolean()).isTrue();
        String access = json.path("accessToken").asText();
        mockMvc.perform(get("/api/trip/owned")
                        .header(JwtBearerConstants.AUTHORIZATION_HEADER,
                                JwtBearerConstants.BEARER_PREFIX + access))
                .andExpect(status().isOk());
    }

    @Test
    void refresh_rotatesAndInvalidatesOldToken() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"testuser","password":"password"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode loginJson = objectMapper.readTree(login.getResponse().getContentAsString());
        String r1 = loginJson.path("refreshToken").asText();

        MvcResult refresh = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + r1 + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String r2 = objectMapper.readTree(refresh.getResponse().getContentAsString()).path("refreshToken").asText();
        assertThat(r2).isNotBlank().isNotEqualTo(r1);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + r1 + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_revokesRefreshToken() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"testuser","password":"password"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String refresh = objectMapper.readTree(login.getResponse().getContentAsString()).path("refreshToken").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}

package by.ladyka.poputka.controllers;

import by.ladyka.poputka.data.repository.UserDocumentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class DocumentControllerTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDocumentRepository userDocumentRepository;

    private String createdDocumentId;
    private LocalDate createdExpirationDate;
    private String createdDescription;
    private String createdType;

    @BeforeEach
    void setUp() {
        createdDocumentId = null;
        createdExpirationDate = null;
        createdDescription = null;
        createdType = null;
    }

    @AfterEach
    void tearDown() {
        if (createdDocumentId != null) {
            userDocumentRepository.deleteById(createdDocumentId);
        }
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void create_thenGetList_shouldContainCreatedDocument() throws Exception {
        JsonNode created = createDocumentAndGetJson();
        createdDocumentId = created.get("id").asText();

        MvcResult result = mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode list = objectMapper.readTree(result.getResponse().getContentAsString());
        Assertions.assertTrue(list.isArray());

        JsonNode found = null;
        for (JsonNode it : list) {
            if (createdDocumentId.equals(it.path("id").asText(null))) {
                found = it;
                break;
            }
        }
        Assertions.assertNotNull(found, "Created document must be present in list");
        assertDocumentDto(found, createdDocumentId, createdType, createdDescription, createdExpirationDate, "DRAFT");
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void submit_shouldMoveStatusToReview() throws Exception {
        JsonNode created = createDocumentAndGetJson();
        createdDocumentId = created.get("id").asText();

        MvcResult submit = mockMvc.perform(post("/api/documents/submit")
                        .param("documentId", createdDocumentId))
                .andExpect(status().isAccepted())
                .andReturn();

        var node = objectMapper.readTree(submit.getResponse().getContentAsString());
        assertDocumentDto(node, createdDocumentId, createdType, createdDescription, createdExpirationDate, "REVIEW");
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "userDetailsService")
    void upload_shouldReturnCreatedFileNames() throws Exception {
        JsonNode created = createDocumentAndGetJson();
        createdDocumentId = created.get("id").asText();
        MockMultipartFile file = new MockMultipartFile("files", "doc.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());

        MvcResult result = mockMvc.perform(multipart("/api/documents/upload/{documentId}", createdDocumentId)
                        .file(file))
                .andExpect(status().isCreated())
                .andReturn();

        List<String> files = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        Assertions.assertEquals(1, files.size());
        Assertions.assertNotNull(files.get(0));
        Assertions.assertFalse(files.get(0).isBlank());
        Assertions.assertTrue(files.get(0).contains("doc.txt"));
    }

    private JsonNode createDocumentAndGetJson() throws Exception {
        createdType = "OTHER";
        createdDescription = "test";
        createdExpirationDate = LocalDate.now().plusDays(10);

        String payload = """
                {
                  "type": "%s",
                  "description": "%s",
                  "expirationDate": "%s"
                }
                """.formatted(createdType, createdDescription, createdExpirationDate);

        MvcResult result = mockMvc.perform(put("/api/documents/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        Assertions.assertNotNull(node.get("id"));
        Assertions.assertFalse(node.get("id").asText().isBlank());
        assertDocumentDto(node, node.get("id").asText(), createdType, createdDescription, createdExpirationDate, "DRAFT");
        return node;
    }

    private static void assertDocumentDto(
            JsonNode node,
            String expectedId,
            String expectedType,
            String expectedDescription,
            LocalDate expectedExpirationDate,
            String expectedStatus
    ) {
        Assertions.assertEquals(expectedId, node.path("id").asText());
        Assertions.assertEquals(expectedType, node.path("type").asText());
        Assertions.assertEquals(expectedDescription, node.path("description").asText());
        Assertions.assertEquals(expectedExpirationDate.toString(), node.path("expirationDate").asText());
        Assertions.assertEquals(expectedStatus, node.path("status").asText());
    }
}

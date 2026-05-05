package by.ladyka.poputka.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Writes springdoc OpenAPI 3 JSON to a file for CI or local use.
 * Run the full test suite without this class (excluded by tag), or explicitly:
 * {@code ./gradlew generateOpenApiJson}
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("openapi-doc")
class OpenApiJsonGenerationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void writeSpringdocOpenApiJson() throws Exception {
        MvcResult result =
                mockMvc.perform(get("/v3/api-docs"))
                        .andExpect(status().isOk())
                        .andReturn();

        String json =
                result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        JsonNode tree = objectMapper.readTree(json);
        assertThat(tree.path("openapi").asText()).isNotBlank();
        assertThat(tree.path("paths").isObject()).isTrue();

        Path out = resolveOutputPath();
        Files.createDirectories(out.getParent());
        Files.writeString(out, json, StandardCharsets.UTF_8);
    }

    private static Path resolveOutputPath() {
        String prop = System.getProperty("openapi.output.file");
        if (prop != null && !prop.isBlank()) {
            return Path.of(prop);
        }
        return Path.of(System.getProperty("user.dir"))
                .resolve("docs")
                .resolve("openapi")
                .resolve("openapi.json");
    }
}

package by.ladyka.poputka.config;

import by.ladyka.poputka.util.StringSanitizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Global trimming of invisible whitespace for incoming/outgoing JSON strings.
 *
 * <p>Also applied to the "service" ObjectMapper bean ({@code jackson2ObjectMapper}) used in this project.</p>
 */
@Configuration
public class StringSanitizationConfiguration {

    @Bean
    JsonMapperBuilderCustomizer trimStringsInHttpJson() {
        return builder -> {
            tools.jackson.databind.module.SimpleModule module = new tools.jackson.databind.module.SimpleModule("trim-invisible-strings");
            module.addDeserializer(String.class, new ToolsTrimmingStringDeserializer());
            builder.addModule(module);
        };
    }

    /**
     * Reuse the same behavior for any manually constructed {@link ObjectMapper}.
     */
    static void registerTrimmingModule(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule("trim-invisible-strings");
        module.addDeserializer(String.class, new FasterXmlTrimmingStringDeserializer());
        mapper.registerModule(module);
    }

    static final class FasterXmlTrimmingStringDeserializer extends com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer<String> {

        FasterXmlTrimmingStringDeserializer() {
            super(String.class);
        }

        @Override
        public String deserialize(com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt)
                throws java.io.IOException {
            String value = p.getValueAsString();
            return StringSanitizer.trimInvisible(value);
        }

        @Override
        public String getNullValue(com.fasterxml.jackson.databind.DeserializationContext ctxt) {
            return null;
        }
    }

    static final class ToolsTrimmingStringDeserializer extends tools.jackson.databind.deser.std.StdScalarDeserializer<String> {

        ToolsTrimmingStringDeserializer() {
            super(String.class);
        }

        @Override
        public String deserialize(tools.jackson.core.JsonParser p, tools.jackson.databind.DeserializationContext ctxt) {
            String value = p.getValueAsString();
            return StringSanitizer.trimInvisible(value);
        }

        @Override
        public String getNullValue(tools.jackson.databind.DeserializationContext ctxt) {
            return null;
        }
    }
}


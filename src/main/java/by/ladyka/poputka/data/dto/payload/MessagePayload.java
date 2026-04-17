package by.ladyka.poputka.data.dto.payload;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Polymorphic message payload for chat messages.
 * Serialized/deserialized with a discriminator field {@code type}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MessagePayload.Message.class, name = "MESSAGE"),
        @JsonSubTypes.Type(value = MessagePayload.Service.class, name = "SERVICE")
})
public sealed interface MessagePayload permits MessagePayload.Message, MessagePayload.Service {

    @JsonTypeName("MESSAGE")
    record Message(String text) implements MessagePayload {}

    @JsonTypeName("SERVICE")
    record Service(String event, String from, String to) implements MessagePayload {}
}


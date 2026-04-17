package by.ladyka.poputka.service.mapper;

import by.ladyka.poputka.data.dto.MessageCreateDto;
import by.ladyka.poputka.data.dto.payload.MessagePayload;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

/**
 * Builds {@code message.payload} for {@link MessageCreateDto}: explicit JSON or legacy {@code content}.
 */
@Component
public class MessagePayloadResolver {

    @Named("messagePayloadFromCreateDto")
    public MessagePayload messagePayloadFromCreateDto(MessageCreateDto dto) {
        MessagePayload payload = dto.getPayload();
        if (payload != null) {
            return payload;
        }
        if (dto.getContent() != null && !dto.getContent().isBlank()) {
            return new MessagePayload.Message(dto.getContent());
        }
        throw new IllegalArgumentException("payload or content is required");
    }
}

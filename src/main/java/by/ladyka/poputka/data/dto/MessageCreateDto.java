package by.ladyka.poputka.data.dto;

import by.ladyka.poputka.data.dto.payload.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageCreateDto {
    String bookingId;
    /**
     * Preferred: structured message payload (json/jsonb).
     */
    MessagePayload payload;

    /**
     * Backward compatible shortcut for plain text messages.
     */
    String content;
}

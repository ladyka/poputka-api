package by.ladyka.poputka.data.dto;

import by.ladyka.poputka.data.enums.MessageStatus;
import by.ladyka.poputka.data.dto.payload.MessagePayload;
import lombok.Data;

@Data
public class MessageDto {
    private String id;
    private String bookingId;
    private Long senderId;
    private MessagePayload payload;
    private MessageStatus messageStatus;
}

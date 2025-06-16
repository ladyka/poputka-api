package by.ladyka.poputka.data.dto;

import by.ladyka.poputka.data.enums.MessageStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class MessageDto {
    private String id;
    private String bookingId;
    private Long senderId;
    private String content;
    private MessageStatus messageStatus;
}

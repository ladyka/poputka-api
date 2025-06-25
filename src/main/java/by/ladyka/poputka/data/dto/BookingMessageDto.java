package by.ladyka.poputka.data.dto;

import by.ladyka.poputka.data.enums.MessageStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class BookingMessageDto {
    private String id;
    private boolean isMyMessage;
    private String content;
    private MessageStatus messageStatus;
    private Instant modifiedDatetime;
}

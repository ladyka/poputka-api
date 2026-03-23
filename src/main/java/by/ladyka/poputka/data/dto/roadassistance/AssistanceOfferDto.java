package by.ladyka.poputka.data.dto.roadassistance;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AssistanceOfferDto {
    private Long id;
    private Long requestId;
    private Long helperId;
    private String helperName;
    private String message;
    private String status;
    private UUID chatId;
    private Long createdDatetime;
    private Long modifiedDatetime;
}

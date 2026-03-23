package by.ladyka.poputka.data.dto.roadassistance;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class AssistanceRequestDto {
    private Long id;
    private Long authorId;
    private String authorName;
    private String carInfo;
    private Long problemTypeId;
    private String problemTypeName;
    private String description;
    private BigDecimal locationLat;
    private BigDecimal locationLon;
    private String address;
    private String status;
    private Long createdDatetime;
    private Long modifiedDatetime;
    private Instant expiresAt;
    private Instant initialExpiresAt;
    private Instant maxExpiresAt;
    private Instant lastActivityAt;
}

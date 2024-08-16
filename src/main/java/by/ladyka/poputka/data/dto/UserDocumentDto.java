package by.ladyka.poputka.data.dto;

import by.ladyka.poputka.data.enums.DocumentStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDocumentDto extends UserDocumentRequestUpdateDto {
    private DocumentStatus status;
}

package by.ladyka.poputka.data.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDocumentRequestUpdateDto extends UserDocumentRequestCreateDto {
    private String id;
}

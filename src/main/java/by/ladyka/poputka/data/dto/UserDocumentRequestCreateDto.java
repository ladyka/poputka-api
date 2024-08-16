package by.ladyka.poputka.data.dto;

import by.ladyka.poputka.data.enums.DocumentType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserDocumentRequestCreateDto {
    private DocumentType type;
    private String description;
    private LocalDate expirationDate;
}

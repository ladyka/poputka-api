package by.ladyka.poputka.service.mapper;

import by.ladyka.poputka.data.dto.UserDocumentDto;
import by.ladyka.poputka.data.entity.UserDocument;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {
    public UserDocumentDto toDto(UserDocument entity) {
        UserDocumentDto dto = new UserDocumentDto();
        dto.setId(entity.getId());
        dto.setType(entity.getDocumentType());
        dto.setStatus(entity.getDocumentStatus());
        dto.setDescription(entity.getDescription());
        dto.setExpirationDate(entity.getExpirationDate());
        return dto;
    }
}

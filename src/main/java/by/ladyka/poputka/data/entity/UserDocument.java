package by.ladyka.poputka.data.entity;

import by.ladyka.poputka.data.enums.DocumentStatus;
import by.ladyka.poputka.data.enums.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "user_documents")
public class UserDocument extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "document_type", nullable = false, length = 36)
    private String type;

    @Column(name = "document_status", nullable = false, length = 10)
    private String status;

    @Column(name = "document_description", nullable = false)
    private String description;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    public DocumentStatus getDocumentStatus() {
        return DocumentStatus.valueOf(status);
    }

    public void setDocumentStatus(DocumentStatus documentStatus) {
        setStatus(documentStatus.name());
    }

    public DocumentType getDocumentType() {
        return DocumentType.valueOf(type);
    }

    public void setDocumentType(DocumentType documentType) {
        type = documentType.name();
    }
}

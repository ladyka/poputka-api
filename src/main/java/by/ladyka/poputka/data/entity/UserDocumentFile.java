package by.ladyka.poputka.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_document_files")
public class UserDocumentFile extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "document_id", nullable = false, length = 36)
    private String documentId;

    @Column(name = "file_url", nullable = false, length = 511)
    private String fileUrl;

    @Column(name = "file_status", nullable = false, length = 10)
    private String status;

}

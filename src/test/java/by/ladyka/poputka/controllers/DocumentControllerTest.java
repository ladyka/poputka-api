package by.ladyka.poputka.controllers;

import by.ladyka.poputka.data.dto.UserDocumentDto;
import by.ladyka.poputka.data.dto.UserDocumentRequestCreateDto;
import by.ladyka.poputka.data.dto.UserDocumentRequestUpdateDto;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.UserDocument;
import by.ladyka.poputka.data.enums.DocumentStatus;
import by.ladyka.poputka.data.enums.DocumentType;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import by.ladyka.poputka.data.repository.UserDocumentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.UUID;

//@SpringBootTest
class DocumentControllerTest {

    @Autowired
    DocumentController documentController;

    @Autowired
    private UserDocumentRepository userDocumentRepository;

    @Autowired
    private PoputkaUserRepository poputkaUserRepository;

    private PoputkaUser user;

    @BeforeEach
    void setUp() {
        user = new PoputkaUser();
        user.setUsername("testuser");
        user = poputkaUserRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        poputkaUserRepository.delete(user);
    }

    // @Test
    void documents() {
        String documentId1 = createTestDocument();
        String documentId2 = createTestDocument();
        String documentId3 = createTestDocument();
        List<UserDocumentDto> documents = documentController.documents(testPrincipal()).getBody();
        assert documents != null;
        Assertions.assertEquals(3, documents.size());
        List<String> ids = documents.stream().map(UserDocumentDto::getId).toList();
        Assertions.assertTrue(ids.contains(documentId1));
        Assertions.assertTrue(ids.contains(documentId2));
        Assertions.assertTrue(ids.contains(documentId3));
        deleteDocument(documentId1);
        deleteDocument(documentId2);
        deleteDocument(documentId3);
    }

    // @Test
    void createDocument() {
        LocalDate date = LocalDate.now();
        String description = "test";
        UserDocumentRequestCreateDto request = new UserDocumentRequestCreateDto();
        request.setDescription(description);
        request.setExpirationDate(date);
        request.setType(DocumentType.OTHER);
        ResponseEntity<UserDocumentDto> response = documentController.createDocument(request, testPrincipal());
        Assertions.assertNotNull(response);
        UserDocumentDto actual = response.getBody();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(date, actual.getExpirationDate());
        Assertions.assertEquals(description, actual.getDescription());
        Assertions.assertEquals(DocumentStatus.DRAFT, actual.getStatus());
        Assertions.assertEquals(DocumentType.OTHER, actual.getType());
        Assertions.assertNotNull(actual.getId());
    }

    // @Test
    void updateDocument() {
        LocalDate date = LocalDate.of(2099, Month.JANUARY, 3);
        String description = "НОВЫЙ ДОКУМЕНТ";
        String documentId = createTestDocument();
        UserDocumentRequestUpdateDto dto = new UserDocumentRequestUpdateDto();
        dto.setId(documentId);
        dto.setType(DocumentType.PASSPORT);
        dto.setExpirationDate(date);
        dto.setDescription(description);
        ResponseEntity<UserDocumentDto> response = documentController.updateDocument(dto, testPrincipal());
        Assertions.assertNotNull(response);
        UserDocumentDto actual = response.getBody();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(date, actual.getExpirationDate());
        Assertions.assertEquals(description, actual.getDescription());
        Assertions.assertEquals(DocumentStatus.DRAFT, actual.getStatus());
        Assertions.assertEquals(DocumentType.PASSPORT, actual.getType());
        Assertions.assertNotNull(actual.getId());

    }

    // @Test
    void submit() {
        String documentId = createTestDocument();
        ResponseEntity<UserDocumentDto> response = documentController.submit(documentId, testPrincipal());
        Assertions.assertNotNull(response);
        UserDocumentDto actual = response.getBody();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(DocumentStatus.REVIEW, actual.getStatus());
    }

    String createTestDocument() {
        UserDocument document = new UserDocument();
        document.setId(UUID.randomUUID().toString());
        document.setDocumentType(DocumentType.OTHER);
        document.setDocumentStatus(DocumentStatus.DRAFT);
        document.setDescription("INIT");
        document.setExpirationDate(LocalDate.of(1990, Month.DECEMBER, 23));
        document.setCreatedUser(user);
        document.setModifiedUser(user);
        document.setCreatedDatetime(Instant.now().toEpochMilli());
        document.setModifiedDatetime(Instant.now().toEpochMilli());
        document = userDocumentRepository.save(document);
        return document.getId();
    }

    void deleteDocument(String id) {
        userDocumentRepository.deleteById(id);
    }

    private Principal testPrincipal() {
        return () -> user.getUsername();
    }

}

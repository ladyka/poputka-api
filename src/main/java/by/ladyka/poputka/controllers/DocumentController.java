package by.ladyka.poputka.controllers;

import by.ladyka.poputka.data.dto.UserDocumentDto;
import by.ladyka.poputka.data.dto.UserDocumentRequestCreateDto;
import by.ladyka.poputka.data.dto.UserDocumentRequestUpdateDto;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.UserDocument;
import by.ladyka.poputka.data.entity.UserDocumentFile;
import by.ladyka.poputka.data.enums.DocumentStatus;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import by.ladyka.poputka.data.repository.UserDocumentFileRepository;
import by.ladyka.poputka.data.repository.UserDocumentRepository;
import by.ladyka.poputka.service.FileService;
import by.ladyka.poputka.service.mapper.DocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {

    private final UserDocumentRepository userDocumentRepository;
    private final UserDocumentFileRepository userDocumentFileRepository;
    private final FileService fileService;
    private final PoputkaUserRepository poputkaUserRepository;
    private final DocumentMapper documentMapper;

    @GetMapping
    public ResponseEntity<List<UserDocumentDto>> documents(
            Principal principal
                                                          ) {
        PoputkaUser user = poputkaUserRepository.findByUsername(principal.getName()).orElseThrow();
        return ResponseEntity.ok(userDocumentRepository.findAllByCreatedUser(user.getUUID()).stream()
                .map(documentMapper::toDto)
                .collect(Collectors.toList()));
    }

    @PutMapping("/create")
    public ResponseEntity<UserDocumentDto> createDocument(
            @RequestBody UserDocumentRequestCreateDto dto,
            Principal principal) {
        if (LocalDate.now().plusDays(1).isAfter(dto.getExpirationDate())) {
            log.warn("Документ уже не действует, нужно добавить на это валидацию!!!");
        }
        PoputkaUser user = poputkaUserRepository.findByUsername(principal.getName()).orElseThrow();

        // Создание нового документа
        UserDocument document = new UserDocument();
        String documentId = UUID.randomUUID().toString();
        document.setId(documentId);
        document.setDocumentType(dto.getType());
        document.setDescription(dto.getDescription());
        document.setExpirationDate(dto.getExpirationDate());
        document.setDocumentStatus(DocumentStatus.DRAFT);

        //TODO AUDIT
        document.setCreated(user);

        // Сохранение документа
        UserDocument savedDocument = userDocumentRepository.save(document);
        return new ResponseEntity<>(documentMapper.toDto(savedDocument), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<UserDocumentDto> updateDocument(
            @RequestBody UserDocumentRequestUpdateDto dto,
            Principal principal) {

        PoputkaUser user = poputkaUserRepository.findByUsername(principal.getName()).orElseThrow();
        UserDocument document = userDocumentRepository.findById(dto.getId()).orElseThrow();
        if (Objects.equals(document.getCreatedUser(), user.getUUID())) {
            if (DocumentStatus.DRAFT.equals(document.getDocumentStatus()) ||
                    (DocumentStatus.DECLINE.equals(document.getDocumentStatus()))) {
                document.setDocumentType(dto.getType());
                document.setDescription(dto.getDescription());
                document.setExpirationDate(dto.getExpirationDate());
                document.setDocumentStatus(DocumentStatus.DRAFT);

                //TODO AUDIT
                document.setModified(user);

                userDocumentRepository.save(document);
                return new ResponseEntity<>(documentMapper.toDto(document), HttpStatus.ACCEPTED);
            } else {
                log.warn("Пользователь {} пытается изменить документ {} который имеет статус {}", user.getUUID(), document.getId(),
                        document.getDocumentStatus());
                return new ResponseEntity<>(documentMapper.toDto(document), HttpStatus.FORBIDDEN);
            }
        } else {
            log.warn("Пользователь {} пытается изменить документ {} который принадлежит {}", user.getUUID(), document.getId(),
                    document.getCreatedUser());
            throw new AccessDeniedException("AccessDenied");
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadDocumentFile(
            String documentId,
            @RequestParam("files") List<MultipartFile> files,
            Principal principal) {
        PoputkaUser user = poputkaUserRepository.findByUsername(principal.getName()).orElseThrow();
        UserDocument savedDocument = userDocumentRepository.findById(documentId).orElseThrow();
        List<String> filesPath = new ArrayList<>();
        for (MultipartFile file : files) {
            UserDocumentFile userDocumentFile = new UserDocumentFile();
            userDocumentFile.setId(UUID.randomUUID().toString());
            userDocumentFile.setDocumentId(savedDocument.getId());
            userDocumentFile.setFileUrl(fileService.saveFile(file, user.getUUID(), documentId));

            //TODO AUDIT
            userDocumentFile.setCreated(user);

            userDocumentFileRepository.save(userDocumentFile);
            filesPath.add(userDocumentFile.getFileUrl());
        }
        return new ResponseEntity<>(filesPath, HttpStatus.CREATED);
    }

    @PostMapping("/submit")
    public ResponseEntity<UserDocumentDto> submit(
            String documentId,
            Principal principal) {
        PoputkaUser user = poputkaUserRepository.findByUsername(principal.getName()).orElseThrow();
        UserDocument document = userDocumentRepository.findById(documentId).orElseThrow();
        if (Objects.equals(document.getCreatedUser(), user.getUUID())) {
            if (DocumentStatus.DRAFT.equals(document.getDocumentStatus()) ||
                    (DocumentStatus.DECLINE.equals(document.getDocumentStatus()))) {
                document.setDocumentStatus(DocumentStatus.REVIEW);

                //TODO AUDIT
                document.setModified(user);

                userDocumentRepository.save(document);
                return new ResponseEntity<>(documentMapper.toDto(document), HttpStatus.ACCEPTED);
            } else {
                log.warn("Пользователь {} пытается подтвердить документ {} который имеет статус {}", user.getUUID(), document.getId(),
                        document.getDocumentStatus());
                return new ResponseEntity<>(documentMapper.toDto(document), HttpStatus.FORBIDDEN);
            }
        } else {
            log.warn("Пользователь {} пытается подтвердить документ {} который принадлежит {}", user.getUUID(), document.getId(),
                    document.getCreatedUser());
            throw new AccessDeniedException("AccessDenied");
        }
    }
}

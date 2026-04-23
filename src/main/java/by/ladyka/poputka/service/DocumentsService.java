package by.ladyka.poputka.service;

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
import by.ladyka.poputka.service.mapper.DocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static by.ladyka.poputka.data.enums.DocumentStatus.REVIEW;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentsService {

    private final UserDocumentRepository userDocumentRepository;
    private final UserDocumentFileRepository userDocumentFileRepository;
    private final FileService fileService;
    private final PoputkaUserRepository poputkaUserRepository;
    private final DocumentMapper documentMapper;

    public List<UserDocumentDto> documents(
            PoputkaUser user
                                                          ) {
        return userDocumentRepository.findAllByCreatedUser(user).stream()
                .map(documentMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserDocumentDto createDocument(
            @RequestBody UserDocumentRequestCreateDto dto) {
        if (LocalDate.now().plusDays(1).isAfter(dto.getExpirationDate())) {
            log.warn("Документ уже не действует, нужно добавить на это валидацию!!!");
        }

        // Создание нового документа
        UserDocument document = new UserDocument();
        document.setDocumentType(dto.getType());
        document.setDescription(dto.getDescription());
        document.setExpirationDate(dto.getExpirationDate());
        document.setDocumentStatus(DocumentStatus.DRAFT);

        // Сохранение документа
        UserDocument savedDocument = userDocumentRepository.save(document);
        return documentMapper.toDto(savedDocument);
    }

    public UserDocumentDto updateDocument(
            @RequestBody UserDocumentRequestUpdateDto dto,
            PoputkaUser user) {

        UserDocument document = userDocumentRepository.findById(dto.getId()).orElseThrow();
        if (Objects.equals(document.getCreatedUser().getId(), user.getId())) {
            if (DocumentStatus.DRAFT.equals(document.getDocumentStatus()) ||
                    (DocumentStatus.DECLINE.equals(document.getDocumentStatus()))) {
                document.setDocumentType(dto.getType());
                document.setDescription(dto.getDescription());
                document.setExpirationDate(dto.getExpirationDate());
                document.setDocumentStatus(DocumentStatus.DRAFT);

                userDocumentRepository.save(document);
                return documentMapper.toDto(document);
            } else {
                log.warn("Пользователь {} пытается изменить документ {} который имеет статус {}", user.getUUID(), document.getId(),
                        document.getDocumentStatus());
                throw new AccessDeniedException("AccessDenied");
            }
        } else {
            log.warn("Пользователь {} пытается изменить документ {} который принадлежит {}", user.getUUID(), document.getId(),
                    document.getCreatedUser());
            throw new AccessDeniedException("AccessDenied");
        }
    }

    public List<String> uploadDocumentFile(
            String documentId,
            List<MultipartFile> files,
            PoputkaUser user) {
        UserDocument savedDocument = userDocumentRepository.findById(documentId).orElseThrow();
        List<String> filesPath = new ArrayList<>();
        for (MultipartFile file : files) {
            UserDocumentFile userDocumentFile = new UserDocumentFile();
            userDocumentFile.setDocumentId(savedDocument.getId());
            userDocumentFile.setFileUrl(fileService.saveFile(file, user.getUUID(), documentId));
            userDocumentFile.setStatus("DRAFT");
            userDocumentFileRepository.save(userDocumentFile);
            filesPath.add(userDocumentFile.getFileUrl());
            savedDocument.setDocumentStatus(DocumentStatus.REVIEW);
        }
        userDocumentRepository.save(savedDocument);
        return filesPath;
    }

    public UserDocumentDto submit(
            String documentId,
            PoputkaUser user) {
        UserDocument document = userDocumentRepository.findById(documentId).orElseThrow();
        if (Objects.equals(document.getCreatedUser().getId(), user.getId())) {
            if (DocumentStatus.DRAFT.equals(document.getDocumentStatus()) ||
                    (DocumentStatus.DECLINE.equals(document.getDocumentStatus()))) {
                document.setDocumentStatus(REVIEW);

                userDocumentRepository.save(document);
                return documentMapper.toDto(document);
            } else {
                log.warn("Пользователь {} пытается подтвердить документ {} который имеет статус {}", user.getUUID(), document.getId(),
                        document.getDocumentStatus());
                throw new AccessDeniedException("AccessDenied");
            }
        } else {
            log.warn("Пользователь {} пытается подтвердить документ {} который принадлежит {}", user.getUUID(), document.getId(),
                    document.getCreatedUser());
            throw new AccessDeniedException("AccessDenied");
        }
    }
}

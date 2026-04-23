package by.ladyka.poputka.controllers;

import by.ladyka.poputka.ApplicationUserDetails;
import by.ladyka.poputka.data.dto.UserDocumentDto;
import by.ladyka.poputka.data.dto.UserDocumentRequestCreateDto;
import by.ladyka.poputka.data.dto.UserDocumentRequestUpdateDto;
import by.ladyka.poputka.service.DocumentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentsService documentsService;

    @GetMapping
    public ResponseEntity<List<UserDocumentDto>> documents(
            @AuthenticationPrincipal ApplicationUserDetails userDetails) {
        return ResponseEntity.ok(documentsService.documents(userDetails.user()));
    }

    @PutMapping("/create")
    public ResponseEntity<UserDocumentDto> createDocument(
            @RequestBody UserDocumentRequestCreateDto dto) {
        return new ResponseEntity<>(documentsService.createDocument(dto), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<UserDocumentDto> updateDocument(
            @RequestBody UserDocumentRequestUpdateDto dto,
            @AuthenticationPrincipal ApplicationUserDetails userDetails) {
        return new ResponseEntity<>(documentsService.updateDocument(dto, userDetails.user()), HttpStatus.ACCEPTED);
    }

    @PostMapping("/upload/{documentId}")
    public ResponseEntity<List<String>> uploadDocumentFile(
            @PathVariable String documentId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal ApplicationUserDetails userDetails) {
        return new ResponseEntity<>(documentsService.uploadDocumentFile(documentId, files, userDetails.user()), HttpStatus.CREATED);
    }

    @PostMapping("/submit")
    public ResponseEntity<UserDocumentDto> submit(
            @RequestParam("documentId") String documentId,
            @AuthenticationPrincipal ApplicationUserDetails userDetails) {
        return new ResponseEntity<>(documentsService.submit(documentId, userDetails.user()), HttpStatus.ACCEPTED);
    }
}

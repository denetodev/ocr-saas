package com.canscan.ocrsaas.controller;

import com.canscan.ocrsaas.dto.DocumentDto;
import com.canscan.ocrsaas.dto.DocumentUpdateDto;
import com.canscan.ocrsaas.exception.FileStorageException;
import com.canscan.ocrsaas.exception.ResourceNotFoundException;
import com.canscan.ocrsaas.model.Document;
import com.canscan.ocrsaas.model.User;
import com.canscan.ocrsaas.repository.DocumentRepository;
import com.canscan.ocrsaas.service.DocumentService;
import com.canscan.ocrsaas.service.FileStorageService;
import com.canscan.ocrsaas.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document management and OCR API")
public class DocumentController {

    private final DocumentService documentService;
    private final UserService userService;
    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;


    @GetMapping
    @Operation(summary = "Get all documents", description = "Get all documents for the current user")
    public ResponseEntity<Page<DocumentDto>> getAllDocuments(Pageable pageable) {
        return ResponseEntity.ok(documentService.getAllDocuments(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document", description = "Get a specific document by ID")
    public ResponseEntity<DocumentDto> getDocument(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocument(id));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download document", description = "Download a document as .docx")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        try {
            Resource resource = documentService.downloadDocument(id);
            String filename = documentService.getDocumentFilename(id);

            String contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            throw new FileStorageException("Could not download file", e);
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload document", description = "Upload a document for OCR processing")
    public ResponseEntity<DocumentDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) Long folderId) throws IOException {
        return ResponseEntity.ok(documentService.uploadAndProcessDocument(file, folderId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update document", description = "Update an existing document")
    public ResponseEntity<DocumentDto> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentUpdateDto updateDto) {
        return ResponseEntity.ok(documentService.updateDocument(id, updateDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document", description = "Delete a document")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

}

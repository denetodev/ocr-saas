package com.canscan.ocrsaas.service;

import com.canscan.ocrsaas.dto.DocumentDto;
import com.canscan.ocrsaas.dto.DocumentUpdateDto;
import com.canscan.ocrsaas.exception.FileStorageException;
import com.canscan.ocrsaas.exception.ResourceNotFoundException;
import com.canscan.ocrsaas.model.Document;
import com.canscan.ocrsaas.model.Folder;
import com.canscan.ocrsaas.model.User;
import com.canscan.ocrsaas.repository.DocumentRepository;
import com.canscan.ocrsaas.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FolderRepository folderRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final OcrService ocrService;
    private final DocService docService;

    public Page<DocumentDto> getAllDocuments(Pageable pageable) {
        User user = userService.getAuthenticatedUser();
        Page<Document> documents = documentRepository.findByUser(user, pageable);
        return documents.map(this::mapToDto);
    }

    public DocumentDto getDocument(Long id) {
        User user = userService.getAuthenticatedUser();
        Document document = documentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        return mapToDto(document);
    }

    @Transactional
    public DocumentDto uploadAndProcessDocument(MultipartFile file, Long folderId) throws IOException {
        User user = userService.getAuthenticatedUser();

        // Store the original file
        String originalFilePath = fileStorageService.storeFile(file, "originals");

        // Create document entity
        Document document = new Document();
        document.setName(file.getOriginalFilename());
        document.setOriginalFilePath(originalFilePath);
        document.setStatus(Document.Status.PENDING);
        document.setUser(user);

        // Set folder if provided
        if (folderId != null) {
            Folder folder = folderRepository.findByIdAndUser(folderId, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));
            document.setFolder(folder);
        }

        // Save document
        document = documentRepository.save(document);

        // Process OCR asynchronously (in a real app, this would be done by a background job)
        processOcr(document.getId());

        return mapToDto(document);
    }

    @Transactional
    public void processOcr(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        try {
            // Update status
            document.setStatus(Document.Status.PROCESSING);
            documentRepository.save(document);

            // Perform OCR
            String extractedText = ocrService.performOcr(document.getOriginalFilePath());
            document.setExtractedText(extractedText);

            // Generate DOC file (placeholder for now)
            String docFilePath = docService.generateDoc(extractedText, document.getName());
            document.setDocFilePath(docFilePath);

            // Update status
            document.setStatus(Document.Status.COMPLETED);
            documentRepository.save(document);
        } catch (Exception e) {
            document.setStatus(Document.Status.FAILED);
            documentRepository.save(document);
            throw new RuntimeException("Failed to process OCR", e);
        }
    }

    @Transactional
    public DocumentDto updateDocument(Long id, DocumentUpdateDto updateDto) {
        User user = userService.getAuthenticatedUser();
        Document document = documentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        document.setName(updateDto.getName());

        if (updateDto.getExtractedText() != null) {
            document.setExtractedText(updateDto.getExtractedText());
        }

        if (updateDto.getFolderId() != null) {
            Folder folder = folderRepository.findByIdAndUser(updateDto.getFolderId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", updateDto.getFolderId()));
            document.setFolder(folder);
        } else {
            document.setFolder(null);
        }

        document = documentRepository.save(document);
        return mapToDto(document);
    }

    @Transactional
    public void deleteDocument(Long id) {
        User user = userService.getAuthenticatedUser();
        Document document = documentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        // Delete files
        if (document.getOriginalFilePath() != null) {
            fileStorageService.deleteFile(document.getOriginalFilePath());
        }

        if (document.getDocFilePath() != null) {
            fileStorageService.deleteFile(document.getDocFilePath());
        }

        // Delete document
        documentRepository.delete(document);
    }

    public Resource downloadDocument(Long id) throws IOException {
        User user = userService.getAuthenticatedUser();
        Document document = documentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        // Check if document has been processed
        if (document.getStatus() != Document.Status.COMPLETED || document.getDocFilePath() == null) {
            throw new IllegalStateException("Document is not ready for download");
        }

        // Get file
        Path filePath = fileStorageService.getFilePath(document.getDocFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        // Check if file exists
        if (!resource.exists()) {
            throw new FileStorageException("File not found: " + document.getDocFilePath());
        }

        return resource;
    }

    public String getDocumentFilename(Long id) {
        User user = userService.getAuthenticatedUser();
        Document document = documentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        String filename = document.getName();
        if (!filename.endsWith(".docx")) {
            filename += ".docx";
        }

        return filename;
    }

    private DocumentDto mapToDto(Document document) {
        return DocumentDto.builder()
                .id(document.getId())
                .name(document.getName())
                .status(document.getStatus().name())
                .folderId(document.getFolder() != null ? document.getFolder().getId() : null)
                .extractedText(document.getExtractedText())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

}

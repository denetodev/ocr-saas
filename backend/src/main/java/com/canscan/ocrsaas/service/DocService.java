package com.canscan.ocrsaas.service;

import com.canscan.ocrsaas.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocService {

    private final FileStorageService fileStorageService;

    public String generateDoc(String text, String originalFileName) {
        try {
            // Create a new document
            XWPFDocument document = new XWPFDocument();

            // Split text into paragraphs
            String[] paragraphs = text.split("\n");

            // Add each paragraph to the document
            for (String paragraph : paragraphs) {
                if (!paragraph.trim().isEmpty()) {
                    XWPFParagraph p = document.createParagraph();
                    XWPFRun run = p.createRun();
                    run.setText(paragraph);
                }
            }

            // Generate a unique filename
            String baseName = originalFileName;
            if (baseName.contains(".")) {
                baseName = baseName.substring(0, baseName.lastIndexOf('.'));
            }
            String docFileName = baseName + "_" + UUID.randomUUID().toString() + ".docx";

            // Create subdirectory
            String subdirectory = "docs";
            Path targetLocation = fileStorageService.getFilePath(subdirectory);

            // Save the document
            String filePath = subdirectory + "/" + docFileName;
            Path fullPath = fileStorageService.getFilePath(filePath);

            try (FileOutputStream out = new FileOutputStream(fullPath.toFile())) {
                document.write(out);
            }

            return filePath;

        } catch (IOException e) {
            throw new FileStorageException("Could not generate DOC file", e);
        }
    }
}

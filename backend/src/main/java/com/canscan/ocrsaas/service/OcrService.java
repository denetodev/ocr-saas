package com.canscan.ocrsaas.service;

import com.canscan.ocrsaas.exception.OcrProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@Slf4j
public class OcrService {
    private final FileStorageService fileStorageService;

    @Value("${app.ocr.data-path:./tessdata}")
    private String tessdataPath;

    public String performOcr(String filePath) {
        try {
            Path path = fileStorageService.getFilePath(filePath);
            File imageFile = path.toFile();

            log.info("Performing OCR on file: {}", imageFile.getAbsolutePath());

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessdataPath);
            tesseract.setLanguage("eng"); // Set language to English

            // You can set other Tesseract parameters here
            // tesseract.setPageSegMode(1);
            // tesseract.setOcrEngineMode(1);

            return tesseract.doOCR(imageFile);

        } catch (TesseractException e) {
            throw new OcrProcessingException("Failed to process OCR for file: " + filePath, e);
        }
    }
}

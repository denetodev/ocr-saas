package com.canscan.ocrsaas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDto {

    private Long id;
    private String name;
    private String status;
    private Long folderId;
    private String extractedText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.canscan.ocrsaas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentUpdateDto {

    @NotBlank(message = "Document name is required")
    @Size(min = 1, max = 255, message = "Document name must be between 1 and 255 characters")
    private String name;

    private String extractedText;

    private Long folderId;

}

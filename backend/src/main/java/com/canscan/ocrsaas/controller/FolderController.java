package com.canscan.ocrsaas.controller;

import com.canscan.ocrsaas.dto.FolderDto;
import com.canscan.ocrsaas.dto.FolderRequestDto;
import com.canscan.ocrsaas.service.FolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
@Tag(name = "Folders", description = "Folder management API")
public class FolderController {

    private final FolderService folderService;

    @GetMapping("/root")
    @Operation(summary = "Get root folders", description = "Get all root folders for the current user")
    public ResponseEntity<List<FolderDto>> getRootFolders() {
        return ResponseEntity.ok(folderService.getRootFolders());
    }

    @GetMapping("/{parentId}/subfolders")
    @Operation(summary = "Get subfolders", description = "Get all subfolders for a specific folder")
    public ResponseEntity<List<FolderDto>> getSubfolders(@PathVariable Long parentId) {
        return ResponseEntity.ok(folderService.getSubfolders(parentId));
    }

    @PostMapping
    @Operation(summary = "Create folder", description = "Create a new folder")
    public ResponseEntity<FolderDto> createFolder(@Valid @RequestBody FolderRequestDto folderRequest) {
        return ResponseEntity.ok(folderService.createFolder(folderRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update folder", description = "Update an existing folder")
    public ResponseEntity<FolderDto> updateFolder(
            @PathVariable Long id,
            @Valid @RequestBody FolderRequestDto folderRequest) {
        return ResponseEntity.ok(folderService.updateFolder(id, folderRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete folder", description = "Delete a folder and all its contents")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long id) {
        folderService.deleteFolder(id);
        return ResponseEntity.noContent().build();
    }

}

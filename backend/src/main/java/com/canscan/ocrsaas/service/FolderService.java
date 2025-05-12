package com.canscan.ocrsaas.service;

import com.canscan.ocrsaas.dto.FolderDto;
import com.canscan.ocrsaas.dto.FolderRequestDto;
import com.canscan.ocrsaas.exception.ResourceNotFoundException;
import com.canscan.ocrsaas.model.Folder;
import com.canscan.ocrsaas.model.User;
import com.canscan.ocrsaas.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserService userService;

    public List<FolderDto> getRootFolders() {
        User user = userService.getAuthenticatedUser();
        List<Folder> folders = folderRepository.findByUserAndParentIsNull(user);
        return folders.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<FolderDto> getSubfolders(Long parentId) {
        User user = userService.getAuthenticatedUser();
        Folder parent = folderRepository.findByIdAndUser(parentId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", parentId));

        List<Folder> folders = folderRepository.findByUserAndParent(user, parent);
        return folders.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public FolderDto createFolder(FolderRequestDto folderRequest) {
        User user = userService.getAuthenticatedUser();

        Folder folder = new Folder();
        folder.setName(folderRequest.getName());
        folder.setUser(user);

        if (folderRequest.getParentId() != null) {
            Folder parent = folderRepository.findByIdAndUser(folderRequest.getParentId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderRequest.getParentId()));
            folder.setParent(parent);
        }

        folder = folderRepository.save(folder);
        return mapToDto(folder);
    }

    @Transactional
    public FolderDto updateFolder(Long id, FolderRequestDto folderRequest) {
        User user = userService.getAuthenticatedUser();
        Folder folder = folderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", id));

        folder.setName(folderRequest.getName());

        if (folderRequest.getParentId() != null) {
            // Prevent circular references
            if (folderRequest.getParentId().equals(id)) {
                throw new IllegalArgumentException("A folder cannot be its own parent");
            }

            Folder parent = folderRepository.findByIdAndUser(folderRequest.getParentId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderRequest.getParentId()));
            folder.setParent(parent);
        } else {
            folder.setParent(null);
        }

        folder = folderRepository.save(folder);
        return mapToDto(folder);
    }

    @Transactional
    public void deleteFolder(Long id) {
        User user = userService.getAuthenticatedUser();
        Folder folder = folderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", id));

        folderRepository.delete(folder);
    }

    @Transactional
    public void createRootFolder(User user) {
        Folder rootFolder = new Folder();
        rootFolder.setName("Root");
        rootFolder.setUser(user);
        folderRepository.save(rootFolder);
    }

    private FolderDto mapToDto(Folder folder) {
        return FolderDto.builder()
                .id(folder.getId())
                .name(folder.getName())
                .parentId(folder.getParent() != null ? folder.getParent().getId() : null)
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }

}

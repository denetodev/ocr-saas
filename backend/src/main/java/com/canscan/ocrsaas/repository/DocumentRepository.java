package com.canscan.ocrsaas.repository;

import com.canscan.ocrsaas.model.Document;
import com.canscan.ocrsaas.model.Folder;
import com.canscan.ocrsaas.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Page<Document> findByUser(User user, Pageable pageable);
    List<Document> findByUserAndFolder(User user, Folder folder);
    Optional<Document> findByIdAndUser(Long id, User user);
}

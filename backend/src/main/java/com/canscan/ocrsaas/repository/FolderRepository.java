package com.canscan.ocrsaas.repository;

import com.canscan.ocrsaas.model.Folder;
import com.canscan.ocrsaas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findByUserAndParentIsNull(User user);
    List<Folder> findByUserAndParent(User user, Folder parent);
    Optional<Folder> findByIdAndUser(Long id, User user);

}

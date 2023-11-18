package com.SecurityBoardEx.BoardEx.file.repository;

import com.SecurityBoardEx.BoardEx.file.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    FileEntity findByFilePath(String filePath);
}

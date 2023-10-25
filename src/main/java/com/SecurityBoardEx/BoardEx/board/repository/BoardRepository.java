package com.SecurityBoardEx.BoardEx.board.repository;

import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Long>, CustomBoardRepository {
    @EntityGraph(attributePaths = {"writer"})
    Optional<BoardEntity> findWithWriterById(Long id);
}

package com.SecurityBoardEx.BoardEx.comment.repository;

import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import com.SecurityBoardEx.BoardEx.comment.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    List<CommentEntity> findAllByBoard(BoardEntity boardEntity);

    List<CommentEntity> findAllByParent(CommentEntity parent);
}

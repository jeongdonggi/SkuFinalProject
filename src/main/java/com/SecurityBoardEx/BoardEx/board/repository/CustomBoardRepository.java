package com.SecurityBoardEx.BoardEx.board.repository;

import com.SecurityBoardEx.BoardEx.board.condition.BoardSearchCondition;
import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomBoardRepository {
    Page<BoardEntity> search(BoardSearchCondition boardSearchCondition, Pageable pageable);
}

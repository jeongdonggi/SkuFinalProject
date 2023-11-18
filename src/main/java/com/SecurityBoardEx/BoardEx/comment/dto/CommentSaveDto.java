package com.SecurityBoardEx.BoardEx.comment.dto;

import com.SecurityBoardEx.BoardEx.comment.entity.CommentEntity;

public record CommentSaveDto(String content) {
    public CommentEntity toEntity(){
        return CommentEntity.builder().content(content).build();
    }
}

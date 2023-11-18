package com.SecurityBoardEx.BoardEx.board.dto;

import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record BoardSaveDto(@NotBlank String title,
                           @NotBlank String content,
                           boolean restrictedAccess,
                           List<MultipartFile> uploadFile){
    public BoardEntity toEntity(){
        return BoardEntity.builder()
                .title(title)
                .content(content)
                .restrictedAccess(restrictedAccess)
                .build();
    }
}

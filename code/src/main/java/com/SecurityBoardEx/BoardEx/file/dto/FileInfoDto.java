package com.SecurityBoardEx.BoardEx.file.dto;

import com.SecurityBoardEx.BoardEx.file.entity.FileEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FileInfoDto {

    private String fileName;

    public FileInfoDto(FileEntity fileEntity){
        this.fileName = fileEntity.getFileName();
    }
}

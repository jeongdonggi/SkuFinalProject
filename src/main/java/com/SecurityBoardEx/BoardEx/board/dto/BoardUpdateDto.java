package com.SecurityBoardEx.BoardEx.board.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
// 여기도 파일 다중으로 받으려면 List

public record BoardUpdateDto(
        Optional<String> title,
        Optional<String> content,
        List<MultipartFile> uploadFile) {
}

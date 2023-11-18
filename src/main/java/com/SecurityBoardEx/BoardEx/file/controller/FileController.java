package com.SecurityBoardEx.BoardEx.file.controller;

import com.SecurityBoardEx.BoardEx.file.dto.FileInfoDto;
import com.SecurityBoardEx.BoardEx.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @DeleteMapping("/file/fileDelete")
    public @ResponseBody String fileDelete(@RequestParam String filePath, Long boardId){
        log.info("fileDelete 진입");
        fileService.deleteFile(filePath, boardId);
        return "완료";
    }

    /** 파일 이름 저장 시 변경 **/
    @GetMapping("/files/{file}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String file) throws UnsupportedEncodingException {

        log.info("file = " + file);

        FileInfoDto fileDto = fileService.findFile(file);
        // DB에서 원본 파일 이름 조회
        String originalFileName = fileDto.getFileName(); // DB에서 가져온 원본 파일 이름
        log.info("originalFileName = " + originalFileName);

        // 파일 이름을 URL 인코딩
        String encodedFileName = URLEncoder.encode(originalFileName, "UTF-8").replace("+", "%20");


        // 헤더에 이름 넣어주기
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);

        Resource fileResource = new FileSystemResource("/files/" + file); // 실제 파일 리소스 로드

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileResource);
    }
}

package com.SecurityBoardEx.BoardEx.file.service;

import com.SecurityBoardEx.BoardEx.board.dto.BoardInfoDto;
import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import com.SecurityBoardEx.BoardEx.board.exception.BoardException;
import com.SecurityBoardEx.BoardEx.board.exception.BoardExceptionType;
import com.SecurityBoardEx.BoardEx.board.repository.BoardRepository;
import com.SecurityBoardEx.BoardEx.board.service.BoardService;
import com.SecurityBoardEx.BoardEx.config.SecurityUtil;
import com.SecurityBoardEx.BoardEx.file.dto.FileInfoDto;
import com.SecurityBoardEx.BoardEx.file.entity.FileEntity;
import com.SecurityBoardEx.BoardEx.file.exception.FileException;
import com.SecurityBoardEx.BoardEx.file.exception.FileExceptionType;
import com.SecurityBoardEx.BoardEx.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FileService {
    private final FileRepository fileRepository;
    private final BoardRepository boardRepository;

    @Value("${file.dir}")
    private String fileDir;

    private static final List<String> RESTRICTED_EXTENSIONS = Arrays.asList(
            ".exe", ".bat", ".com", ".cmd", ".pif", ".scr", // 실행 파일
            ".js", ".vbs", ".ps1", ".sh", // 스크립트와 셀 코드
            ".docm", ".xlsm", ".pptm", // 매크로가 포함된 문서
            ".dll", ".sys", ".cpl", ".jar", ".py", // 스크립트
            ".php", ".asp", ".aspx", ".shtml", ".jsp", ".htm", ".html" // 웹 파일
    );

    public boolean isImageFile(MultipartFile multipartFile){
        String contentType = multipartFile.getContentType();
        return contentType != null && contentType.startsWith("image");
    }

    public String save(MultipartFile multipartFile, String fileName) throws FileException{
        String extension = fileName != null ? fileName.substring(fileName.lastIndexOf(".")) : ""; // 확장자 추출

        String filePath = fileDir + UUID.randomUUID() + extension;
        try {
            multipartFile.transferTo(new File(filePath));

        } catch (IOException e){
            throw new FileException(FileExceptionType.FILE_CAN_NOT_SAVE);
        }

        return filePath;
    }

    /** 게시판에 파일 저장 **/
    public void boardSaveFile(List<MultipartFile> newFiles, BoardEntity board){
        for(MultipartFile file : newFiles){
            String fileName = file.getOriginalFilename();
            if (fileName != "") {
                checkFileExtension(fileName);
                String savedFilePath = save(file, fileName);
                String savedFileName = fileName;
                boolean imageFile = isImageFile(file);

                FileEntity fileEntity = new FileEntity(board, savedFilePath, savedFileName, imageFile);
                board.addFile(fileEntity);
            }
        }
    }

    /** 파일 삭제 **/
    public void deleteFile(String filePath, Long boardId){
        String originalFilePath = fileDir + filePath; // 원래 데이터로 변경

        System.out.println("originalFilePath = " + originalFilePath);

        File file = new File(originalFilePath);

        if(!file.exists()) return; // 존재하지 않아서 안지워도 됨
        if(!file.delete()) throw new FileException(FileExceptionType.FILE_CAN_NOT_DELETE);

        System.out.println(" 파일 삭제 통과");

        BoardEntity board = boardRepository.findById(boardId).orElseThrow(() ->
                new BoardException(BoardExceptionType.BOARD_NOT_FOUND));

        checkAuthority(board, BoardExceptionType.NOT_AUTHORITY_UPDATE_BOARD);

        FileEntity targetFileEntity = board.getFileEntityList().stream()
                .filter(fileEntity -> fileEntity.getFilePath().equals(originalFilePath))
                .findFirst()
                .orElse(null);

        if (targetFileEntity != null) {
            board.getFileEntityList().remove(targetFileEntity);
            //fileRepository.delete(targetFileEntity);  // JPA 영속성 때문에 Entity를 변경하면 db에도 적용 됨
        }
    }

    public void delete(String filePath){
        File file = new File(filePath);

        if(!file.exists()) return; // 존재하지 않아서 안지워도 됨

        if(!file.delete()) throw new FileException(FileExceptionType.FILE_CAN_NOT_DELETE);
    }

    /** 파일 찾기 **/
    public FileInfoDto findFile(String filePath){
        String originalFilePath = fileDir + filePath; // 원래 데이터로 변경
        FileEntity file = fileRepository.findByFilePath(originalFilePath);
        FileInfoDto fileInfoDto = new FileInfoDto(file);
        return fileInfoDto;
    }

    private void checkAuthority(BoardEntity board, BoardExceptionType boardExceptionType){
        if(!board.getWriter().getUsername().equals(SecurityUtil.getLoginUsername()))
            throw new BoardException(boardExceptionType);
    }

    /** 파일 체크 메서드 **/
    private void checkFileExtension(String fileName) throws FileException {
        String extension = fileName != null ? fileName.substring(fileName.lastIndexOf(".")).toLowerCase() : "";
        if (RESTRICTED_EXTENSIONS.contains(extension)) {
            throw new FileException(FileExceptionType.FILE_RESTRICTED);
        }
    }
}

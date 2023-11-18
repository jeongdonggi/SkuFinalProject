package com.SecurityBoardEx.BoardEx.board.dto;

import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import com.SecurityBoardEx.BoardEx.comment.dto.CommentInfoDto;
import com.SecurityBoardEx.BoardEx.comment.entity.CommentEntity;
import com.SecurityBoardEx.BoardEx.file.entity.FileEntity;
import com.SecurityBoardEx.BoardEx.login.dto.UserInfoDto;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter @Setter
@ToString
@NoArgsConstructor
public class BoardInfoDto {

    private Long boardId; // Board의 id
    private String title;
    private String content;
    private List<String> filePath = new ArrayList<>();
    private List<String> fileName = new ArrayList<>();

    private List<Boolean> isImgFile = new ArrayList<>();

    private UserInfoDto writerDto; // 작성자 정보

    private List<CommentInfoDto> commentInfoDtoList;

    public BoardInfoDto(BoardEntity board){
        this.boardId = board.getId();
        this.title = board.getTitle();
        this.content = board.getContent();

        this.writerDto = new UserInfoDto(board.getWriter());

        this.filePath = board.getFileEntityList().stream()
                .map(FileEntity::getFilePath)
                .map(path -> path.replace("C:\\files\\", "")) // 여기에서 변환 로직을 추가합니다.
                .collect(Collectors.toList());

        this.fileName = board.getFileEntityList().stream()
                .map(FileEntity::getFileName)
                .collect(Collectors.toList());

        this.isImgFile = board.getFileEntityList().stream()
                .map(FileEntity::isImage)
                .collect(Collectors.toList());

        // 댓글과 대댓글을 그룹짓기, post.getCommentList()는 댓글과 대댓글이 모두 조회된다.
        Map<CommentEntity, List<CommentEntity>> commentListMap = board.getCommentEntityList().stream()
                .filter(comment -> comment.getParent() != null) // 대댓글만 가져옴
                .collect(Collectors.groupingBy(CommentEntity::getParent)); // 댓글 , 대댓글의 형식으로 그룹핑

        // 댓글과 대댓글을 통해 CommentInfoDto 생성
        commentInfoDtoList = commentListMap.keySet().stream() // keyset: 댓글
                .map(comment -> new CommentInfoDto(comment, commentListMap.get(comment))) // dto로 변경
                .toList();
    }
}

package com.SecurityBoardEx.BoardEx.comment.dto;

import com.SecurityBoardEx.BoardEx.comment.entity.CommentEntity;
import com.SecurityBoardEx.BoardEx.login.dto.UserInfoDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter @Setter
@ToString
public class CommentInfoDto {

    private final static String DEFAULT_DELETE_MESSAGE = "삭제된 댓글입니다.";

    private Long boardId;

    private Long commentId; // 댓글 Id
    private String content; // 내용
    private boolean isRemoved; // 삭제 유무

    private UserInfoDto writerDto; // 작성자 정보
    private List<ReCommentInfoDto> reCommentInfoDtoList; // 대댓글

    // 삭제 되었을 시 삭제 되었다고 알림
    public CommentInfoDto(CommentEntity comment, List<CommentEntity> reCommentList){
        this.boardId = comment.getBoard().getId();
        this.commentId = comment.getId();

        this.content = comment.getContent();

        if(comment.isRemoved()){
            this.content = DEFAULT_DELETE_MESSAGE;
        }

        this.isRemoved = comment.isRemoved();
        this.writerDto = new UserInfoDto(comment.getWriter());
        this.reCommentInfoDtoList = reCommentList.stream().map(ReCommentInfoDto::new).toList();
    }
}

package com.SecurityBoardEx.BoardEx.comment.dto;

import com.SecurityBoardEx.BoardEx.comment.entity.CommentEntity;
import com.SecurityBoardEx.BoardEx.login.dto.UserInfoDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@ToString
public class ReCommentInfoDto {
    private final static String DEFAULT_DELETE_MESSAGE = "삭제된 댓글입니다.";

    private Long boardId;
    private Long parentId;

    private Long reCommentId; // 댓글 Id
    private String content; // 내용
    private LocalDateTime createTime;
    private boolean isRemoved; // 삭제 유무

    private UserInfoDto writerDto; // 작성자 정보

    // 삭제 되었을 시 삭제 되었다고 알림
    public ReCommentInfoDto(CommentEntity reComment){
        this.boardId = reComment.getBoard().getId();
        this.parentId = reComment.getParent().getId();
        this.reCommentId = reComment.getId();
        this.content = reComment.getContent();
        this.createTime = reComment.getCreatedDate();

        if(reComment.isRemoved()){
            this.content = DEFAULT_DELETE_MESSAGE;
        }

        this.isRemoved = reComment.isRemoved();
        this.writerDto = new UserInfoDto(reComment.getWriter());
    }
}

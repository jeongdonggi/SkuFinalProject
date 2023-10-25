package com.SecurityBoardEx.BoardEx.board.dto;

import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
@NoArgsConstructor
public class BriefBoardInfo {

    private Long boardId;

    private String title; //제목
    private String content;
    private String writerName;
    private String createdDate;

    public BriefBoardInfo(BoardEntity board){
        this.boardId = board.getId();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.writerName = board.getWriter().getNickname();
        this.createdDate = board.getCreatedDate().toString();
    }
}

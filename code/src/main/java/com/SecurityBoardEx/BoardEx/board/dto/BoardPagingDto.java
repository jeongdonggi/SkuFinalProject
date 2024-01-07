package com.SecurityBoardEx.BoardEx.board.dto;

import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@ToString
@NoArgsConstructor
public class BoardPagingDto {

    private int totalPageCount;//총 몇페이지가 존재하는지
    private int currentPageNum;//현재 몇 페이지인지( 0부터 시작 )
    private long totalElementCount; //존재하는 게시글의 총 개수
    private int currentPageElementCount; //현재 페이지에 존재하는 게시글 수


    private List<BriefBoardInfo> simpleLectureDtoList = new ArrayList<>();

    public BoardPagingDto(Page<BoardEntity> searchResults) {
        this.totalPageCount = searchResults.getTotalPages();
        this.currentPageNum = searchResults.getNumber();
        this.totalElementCount = searchResults.getTotalElements();
        this.currentPageElementCount = searchResults.getNumberOfElements();
        this.simpleLectureDtoList = searchResults.getContent().stream().map(BriefBoardInfo::new).toList();
    }
}

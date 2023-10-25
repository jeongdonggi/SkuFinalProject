package com.SecurityBoardEx.BoardEx.board.controller;

import com.SecurityBoardEx.BoardEx.board.dto.BoardInfoDto;
import com.SecurityBoardEx.BoardEx.board.dto.BoardPagingDto;
import com.SecurityBoardEx.BoardEx.board.service.BoardService;
import com.SecurityBoardEx.BoardEx.comment.dto.CommentInfoDto;
import com.SecurityBoardEx.BoardEx.comment.dto.CommentSaveDto;
import com.SecurityBoardEx.BoardEx.comment.entity.CommentEntity;
import com.SecurityBoardEx.BoardEx.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
@Slf4j
public class BoardViewController {

    private final BoardService boardService;

    private final CommentService commentService;

    // 게시글 저장
    @GetMapping("/save")
    public String savePage(){
        log.info("get save 들어옴");
        return "board/save";
    }

    // 게시글 수정
    @GetMapping("/update/{id}")
    public String updatePage(@PathVariable Long id, Model model){
        BoardInfoDto boardInfoDto = boardService.getBoardInfo(id);
        log.info("get update 들어옴 {}, {}", id, boardInfoDto.getTitle());
        model.addAttribute("boardId", id);
        model.addAttribute("board", boardInfoDto);
        return "board/update";
    }

    /** 게시글 조회 **/
    @GetMapping("/detail/{boardId}")
    public String getInfo(@PathVariable("boardId") Long boardId, Model model,
                          @PageableDefault(page = 1) Pageable pageable){
        BoardInfoDto boardInfoDto = boardService.getBoardInfo(boardId);

        List<CommentInfoDto> commentList = commentService.findAll(boardId);// 댓글 목록 가져오기

        model.addAttribute("board", boardInfoDto);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("commentList", commentList);
        return "board/detail";
    }

    // /board/gaging?page=1
    @GetMapping("/paging")
    public String paging(@PageableDefault(page = 1) Pageable pageable, Model model){
        BoardPagingDto boardPagingList = boardService.getBoardList(pageable);

        int blockLimit = 5;
        int startPage = ((boardPagingList.getCurrentPageNum() / blockLimit) * blockLimit) + 1;
        int endPage = Math.min(startPage + blockLimit - 1, boardPagingList.getTotalPageCount());

        model.addAttribute("boardList", boardPagingList);
        model.addAttribute("startPage" , startPage);
        model.addAttribute("endPage", endPage);

        return "board/paging";
    }
}

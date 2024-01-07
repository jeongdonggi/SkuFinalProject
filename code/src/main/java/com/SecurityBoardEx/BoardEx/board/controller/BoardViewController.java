package com.SecurityBoardEx.BoardEx.board.controller;

import com.SecurityBoardEx.BoardEx.board.dto.BoardInfoDto;
import com.SecurityBoardEx.BoardEx.board.dto.BoardPagingDto;
import com.SecurityBoardEx.BoardEx.board.service.BoardService;
import com.SecurityBoardEx.BoardEx.comment.dto.CommentInfoDto;
import com.SecurityBoardEx.BoardEx.comment.service.CommentService;
import com.SecurityBoardEx.BoardEx.login.dto.UserInfoDto;
import com.SecurityBoardEx.BoardEx.login.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
@Slf4j
public class BoardViewController {

    private final BoardService boardService;

    private final CommentService commentService;
    private final UserService userService;

    // 게시글 저장
    @GetMapping("/save")
    public String savePage() throws Exception{
        log.info("get save 들어옴");
        UserInfoDto myInfo = userService.getMyInfo(); // 내 정보 찾기
        String role = myInfo.getRole(); // role 찾기

        if(role == "ADMIN" || role == "MANAGER"){
            return "board/saveAuth";
        }
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

        List<String> formatTime = boardService.getFormatTime(boardPagingList); // 시간 포멧팅

        int blockLimit = 5;
        int startPage = ((boardPagingList.getCurrentPageNum() / blockLimit) * blockLimit) + 1;
        int endPage = Math.min(startPage + blockLimit - 1, boardPagingList.getTotalPageCount());

        model.addAttribute("boardList", boardPagingList);
        model.addAttribute("startPage" , startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("formatTime", formatTime);

        return "board/paging";
    }
}

package com.SecurityBoardEx.BoardEx.board.controller;

import com.SecurityBoardEx.BoardEx.board.condition.BoardSearchCondition;
import com.SecurityBoardEx.BoardEx.board.dto.BoardInfoDto;
import com.SecurityBoardEx.BoardEx.board.dto.BoardPagingDto;
import com.SecurityBoardEx.BoardEx.board.dto.BoardSaveDto;
import com.SecurityBoardEx.BoardEx.board.dto.BoardUpdateDto;
import com.SecurityBoardEx.BoardEx.board.service.BoardService;
import com.SecurityBoardEx.BoardEx.file.dto.FileInfoDto;
import com.SecurityBoardEx.BoardEx.file.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;
    private final FileService fileService;

    /** 게시글 저장 **/
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute BoardSaveDto boardSaveDto){
        log.info("post save 진입");
        Long saveId = boardService.save(boardSaveDto);
        log.info("saveId : {}", saveId);
        return "redirect:/board/detail/" +saveId ;
    }

    /** 게시글 수정 **/
    @PostMapping("/update")
    public String update(@RequestParam Long boardId, Model model,
                       @Valid @ModelAttribute BoardUpdateDto boardUpdateDto,
                         @PageableDefault(page = 1) Pageable pageable){
        log.info("post update 들어옴");
        boardService.updated(boardId, boardUpdateDto);
        BoardInfoDto boardInfo = boardService.getBoardInfo(boardId);
        log.info("boardInfo.getTitle {}", boardInfo.getTitle());
        return "redirect:/board/detail/" + boardId;
    }

    /** 게시글 삭제 **/
    @GetMapping("/delete/{boardId}")
    public String delete(@PathVariable("boardId") Long boardId){
        boardService.delete(boardId);
        return "redirect:/board/paging";
    }

    /** 게시글 검색 **/
    @GetMapping("/search")
    public String search(@PageableDefault(page = 1) Pageable pageable, Model model,
                                 @RequestParam String searchWord, @RequestParam String searchType){

        BoardSearchCondition condition = new BoardSearchCondition();

        if("title".equals(searchType)){
            condition.setTitle(searchWord);
        } else if("content".equals(searchType)){
            condition.setContent(searchWord);
        } else if("both".equals(searchType)){
            condition.setTitle(searchWord);
            condition.setContent(searchWord);
        }

        BoardPagingDto boardList = boardService.getBoardList(pageable, condition);

        int blockLimit = 5;
        int startPage = ((boardList.getCurrentPageNum() / blockLimit) * blockLimit) + 1;
        int endPage = Math.min(startPage + blockLimit - 1, boardList.getTotalPageCount());

        model.addAttribute("boardList", boardList);
        model.addAttribute("startPage" , startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("searchWord", searchWord);
        model.addAttribute("searchType", searchType);

        return "board/searchpaging";
    }
}

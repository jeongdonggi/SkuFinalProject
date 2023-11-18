package com.SecurityBoardEx.BoardEx.board.controller;

import com.SecurityBoardEx.BoardEx.board.condition.BoardSearchCondition;
import com.SecurityBoardEx.BoardEx.board.dto.*;
import com.SecurityBoardEx.BoardEx.board.service.BoardService;
import com.SecurityBoardEx.BoardEx.file.service.FileService;
import com.SecurityBoardEx.BoardEx.login.dto.UserInfoDto;
import com.SecurityBoardEx.BoardEx.login.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;
    private final UserService userService;

    /** 게시글 저장 **/
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute BoardSaveDto boardSaveDto){
        log.info("post save 진입");
        log.info("restriced : {}", boardSaveDto.restrictedAccess());
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

        log.info("searchType : {}", searchType );

        if("title".equals(searchType)){
            condition.setTitle(searchWord);
        } else if("content".equals(searchType)){
            condition.setContent(searchWord);
        } else if("both".equals(searchType)){
            condition.setTitle(searchWord);
            condition.setContent(searchWord);
        }

        log.info("condition : {}", condition.toString());

        BoardPagingDto boardList = boardService.getBoardList(pageable, condition);
        List<String> formatTime = boardService.getFormatTime(boardList);

        log.info(boardList.toString());

        int blockLimit = 5;
        int startPage = ((boardList.getCurrentPageNum() / blockLimit) * blockLimit) + 1;
        int endPage = Math.min(startPage + blockLimit - 1, boardList.getTotalPageCount());

        model.addAttribute("boardList", boardList);
        model.addAttribute("startPage" , startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("searchWord", searchWord);
        model.addAttribute("searchType", searchType);
        model.addAttribute("formatTime", formatTime);

        return "board/searchpaging";
    }

    /** 게시글 권한 설정 **/
    @PostMapping("/check-access")
    public ResponseEntity<?> checkBoardAccess(@RequestBody BoardAccessDto boardId) throws Exception {
        try {
            log.info("check 진입");
            UserInfoDto myInfo = userService.getMyInfo();
            String myRole = myInfo.getRole();

            BoardInfoDto boardInfo = boardService.getBoardInfo(boardId.getBoardId());
            UserInfoDto writerDto = boardInfo.getWriterDto();
            String boardRole = writerDto.getRole();

            log.info("role : {}{}", myRole, boardRole);

            // board 찾아서 true일 때 게시글을 쓴 사용자가 manager면 manager, admin이 들어갈 수 있음
            // board 찾아서 true일 때 게시글을 쓴 사용자가 admin이면 admain만 볼 수 있음
            boolean compareAuth = boardService.compareAuth(myRole, boardRole);

            log.info("compareAuth : {}", compareAuth);

            if(compareAuth){
                return ResponseEntity.ok().build();
            }else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            log.error("Error checking access", e);
            return ResponseEntity.internalServerError().body("Error checking access");
        }
    }
}

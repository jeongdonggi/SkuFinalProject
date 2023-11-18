package com.SecurityBoardEx.BoardEx.comment.controllrer;

import com.SecurityBoardEx.BoardEx.comment.dto.CommentInfoDto;
import com.SecurityBoardEx.BoardEx.comment.dto.CommentSaveDto;
import com.SecurityBoardEx.BoardEx.comment.dto.CommentUpdateDto;
import com.SecurityBoardEx.BoardEx.comment.dto.ReCommentInfoDto;
import com.SecurityBoardEx.BoardEx.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comment/{boardId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentInfoDto commentSave(@PathVariable("boardId") Long boardId,
                                      CommentSaveDto commentSaveDto){
        log.info("commentSaveDto: {}" , commentSaveDto);
        CommentInfoDto saveComment = commentService.save(boardId, commentSaveDto);
        log.info("saveComment: {}" , saveComment);
        return saveComment;
    }

    @PostMapping("/comment/{boardId}/{commentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ReCommentInfoDto reCommentSave(@PathVariable("boardId") Long boardId,
                              @PathVariable("commentId") Long commentId,
                              CommentSaveDto commentSaveDto){
        ReCommentInfoDto saveReComment = commentService.saveReComment(boardId, commentId, commentSaveDto);
        return saveReComment;
    }

    @PutMapping("/comment/edit/{commentId}")
    public CommentInfoDto update(@PathVariable("commentId") Long commentId,
                      CommentUpdateDto commentUpdateDto){
        CommentInfoDto updateComment = commentService.update(commentId, commentUpdateDto);
        return updateComment;
    }

    @DeleteMapping("/comment/delete/{commentId}")
    public void delete(@PathVariable("commentId") Long commentId){
        commentService.remove(commentId);
    }
}

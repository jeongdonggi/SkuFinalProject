package com.SecurityBoardEx.BoardEx.comment.service;

import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import com.SecurityBoardEx.BoardEx.board.exception.BoardException;
import com.SecurityBoardEx.BoardEx.board.exception.BoardExceptionType;
import com.SecurityBoardEx.BoardEx.board.repository.BoardRepository;
import com.SecurityBoardEx.BoardEx.comment.dto.CommentInfoDto;
import com.SecurityBoardEx.BoardEx.comment.dto.CommentSaveDto;
import com.SecurityBoardEx.BoardEx.comment.dto.CommentUpdateDto;
import com.SecurityBoardEx.BoardEx.comment.dto.ReCommentInfoDto;
import com.SecurityBoardEx.BoardEx.comment.entity.CommentEntity;
import com.SecurityBoardEx.BoardEx.comment.exception.CommentException;
import com.SecurityBoardEx.BoardEx.comment.exception.CommentExceptionType;
import com.SecurityBoardEx.BoardEx.comment.repository.CommentRepository;
import com.SecurityBoardEx.BoardEx.config.SecurityUtil;
import com.SecurityBoardEx.BoardEx.login.exception.UserException;
import com.SecurityBoardEx.BoardEx.login.exception.UserExceptionType;
import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    public CommentInfoDto save(Long postId, CommentSaveDto commentSaveDto){
        log.info("CommentService.save ");
        CommentEntity comment = commentSaveDto.toEntity();

        comment.confirmWriter(userRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(()->
                new UserException(UserExceptionType.NOT_FOUND_USER)));
        comment.confirmBoard(boardRepository.findById(postId).orElseThrow(()->
                new BoardException(BoardExceptionType.BOARD_NOT_FOUND)));
        commentRepository.save(comment);

        List<CommentEntity> reComments = commentRepository.findAllByParent(comment);
        return new CommentInfoDto(comment, reComments);
    }

    public ReCommentInfoDto saveReComment(Long postId, Long parentId, CommentSaveDto commentSaveDto){
        CommentEntity comment = commentSaveDto.toEntity();

        comment.confirmWriter(userRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(()->
                new UserException(UserExceptionType.NOT_FOUND_USER)));
        comment.confirmBoard(boardRepository.findById(postId).orElseThrow(()->
                new BoardException(BoardExceptionType.BOARD_NOT_FOUND)));
        comment.confirmParent(commentRepository.findById(parentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)));
        commentRepository.save(comment);

        CommentEntity parentComment = commentRepository.findById(parentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT));
        List<CommentEntity> childComment = commentRepository.findAllByParent(parentComment); // 자식들 받아옴

        CommentEntity reCommentEntity = childComment.get(childComment.size() - 1);

        return new ReCommentInfoDto(reCommentEntity);
    }

    public CommentInfoDto update(Long id, CommentUpdateDto commentUpdateDto){
        CommentEntity comment = commentRepository.findById(id).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT));
        if(!comment.getWriter().getUsername().equals(SecurityUtil.getLoginUsername())){
            throw new CommentException(CommentExceptionType.NOT_AUTHORITY_UPDATE_COMMENT);
        }
        commentUpdateDto.content().ifPresent(comment::updateContent); // 이떄 더티체킹해서 db 변경됨
        // 대댓글 리스트를 가져오는 로직 추가
        List<CommentEntity> reComments = commentRepository.findAllByParent(comment);

        return new CommentInfoDto(comment, reComments);
    }

    public void remove(Long id) throws CommentException{
        CommentEntity comment = commentRepository.findById(id).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT));

        if(!comment.getWriter().getUsername().equals(SecurityUtil.getLoginUsername())){
            throw new CommentException(CommentExceptionType.NOT_AUTHORITY_DELETE_COMMENT);
        }

        comment.remove();
        List<CommentEntity> removableCommentList = comment.findRemovableList();
        commentRepository.deleteAll(removableCommentList);
    }

    public List<CommentInfoDto> findAll(Long BoardId) {
        BoardEntity boardEntity = boardRepository.findById(BoardId).orElseThrow(() ->
                new BoardException(BoardExceptionType.BOARD_NOT_FOUND));

        List<CommentEntity> allComments = commentRepository.findAllByBoard(boardEntity); // 전체 가져오기

        Map<CommentEntity, List<CommentEntity>> commentMap = new LinkedHashMap<>();  // 주 댓글을 키로, 대댓글 리스트를 값으로 가지는 맵

        for (CommentEntity comment : allComments) {
            if (comment.getParent() == null) {
                // 주 댓글인 경우
                if (!commentMap.containsKey(comment)) {
                    commentMap.put(comment, new ArrayList<>());
                }
            } else {
                // 대댓글인 경우
                commentMap.get(comment.getParent()).add(comment);
            }
        }

        return commentMap.entrySet().stream().map(entry ->
                new CommentInfoDto(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }
}

package com.SecurityBoardEx.BoardEx.comment.service;

import com.SecurityBoardEx.BoardEx.board.dto.BoardSaveDto;
import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import com.SecurityBoardEx.BoardEx.board.exception.BoardException;
import com.SecurityBoardEx.BoardEx.board.exception.BoardExceptionType;
import com.SecurityBoardEx.BoardEx.board.repository.BoardRepository;
import com.SecurityBoardEx.BoardEx.comment.dto.CommentSaveDto;
import com.SecurityBoardEx.BoardEx.comment.dto.CommentUpdateDto;
import com.SecurityBoardEx.BoardEx.comment.entity.CommentEntity;
import com.SecurityBoardEx.BoardEx.comment.exception.CommentException;
import com.SecurityBoardEx.BoardEx.comment.exception.CommentExceptionType;
import com.SecurityBoardEx.BoardEx.comment.repository.CommentRepository;
import com.SecurityBoardEx.BoardEx.exception.BaseExceptionType;
import com.SecurityBoardEx.BoardEx.login.dto.UserSignUpDto;
import com.SecurityBoardEx.BoardEx.login.entity.Role;
import com.SecurityBoardEx.BoardEx.login.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CommentServiceTest {


    @Autowired
    CommentService commentService;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    UserService userService;
    @Autowired
    BoardRepository boardRepository;

    @PersistenceContext
    EntityManager em;

    private void clear(){
        em.flush();
        em.clear();
    }

    @BeforeEach
    private void signUpAndSetAuthentication() throws Exception {
        userService.signUp(new UserSignUpDto("USERNAME","PASSWORD","name"));
        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        emptyContext.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        User.builder()
                                .username("USERNAME")
                                .password("PASSWORD")
                                .roles(Role.USER.toString())
                                .build(),
                        null)
        );
        SecurityContextHolder.setContext(emptyContext);
        clear();
    }

    private void anotherSignUpAndSetAuthentication() throws Exception {
        userService.signUp(new UserSignUpDto("USERNAME1","PASSWORD123","name"));
        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        emptyContext.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        User.builder()
                                .username("USERNAME1")
                                .password("PASSWORD123")
                                .roles(Role.USER.toString())
                                .build(),
                        null)
        );
        SecurityContextHolder.setContext(emptyContext);
        clear();
    }

    private Long saveBoard(){
        // given
        String title = "제목";
        String content = "내용";
        List<MultipartFile> files = null;
        BoardSaveDto boardSaveDto = new BoardSaveDto(title, content, files);

        // when
        BoardEntity save = boardRepository.save(boardSaveDto.toEntity());
        clear();
        return save.getId();
    }

    private Long saveComment(){
        CommentSaveDto commentSaveDto = new CommentSaveDto("댓글");
        commentService.save(saveBoard(),commentSaveDto);
        clear();

        List<CommentEntity> resultList =
                em.createQuery("select c from CommentEntity  c order by c.createdDate desc ", CommentEntity.class)
                        .getResultList();

        return resultList.get(0).getId();
    }

    private Long saveReComment(Long parentId){
        CommentSaveDto commentSaveDto = new CommentSaveDto("대댓글");
        commentService.saveReComment(saveBoard(), parentId, commentSaveDto);
        clear();

        List<CommentEntity> resultList =
                em.createQuery("select c from CommentEntity c order by c.createdDate desc ", CommentEntity.class)
                        .getResultList();

        return resultList.get(0).getId();
    }


    // 댓글을 삭제하는 경우
    // 대댓글이 남아있는 경우
    // DB와 화면에서는 지워지지 않고, "삭제된 댓글입니다"라고 표시
    @Test
    public void 댓글삭제_대댓글이_남아있는_경우() throws Exception {
        //given
        Long commentId = saveComment();
        saveReComment(commentId);
        saveReComment(commentId);
        saveReComment(commentId);
        saveReComment(commentId);

        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)));

        //when
        commentService.remove(commentId);
        clear();


        //then
        CommentEntity findComment = commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT));
        assertThat(findComment).isNotNull();
        assertThat(findComment.isRemoved()).isTrue();
        assertThat(findComment.getChildList().size()).isEqualTo(4);
    }




    // 댓글을 삭제하는 경우
    //대댓글이 아예 존재하지 않는 경우 : 곧바로 DB에서 삭제
    @Test
    public void 댓글삭제_대댓글이_없는_경우() throws Exception {
        //given
        Long commentId = saveComment();

        //when
        commentService.remove(commentId);
        clear();

        //then
        assertThat(commentRepository.findAll().size()).isSameAs(0);
        assertThat( assertThrows(Exception.class, () -> commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))));
    }




    // 댓글을 삭제하는 경우
    // 대댓글이 존재하나 모두 삭제된 경우
    //댓글과, 달려있는 대댓글 모두 DB에서 일괄 삭제, 화면상에도 표시되지 않음
    @Test
    public void 댓글삭제_대댓글이_존재하나_모두_삭제된_대댓글인_경우() throws Exception {
        //given
        Long commentId = saveComment();
        Long reCommend1Id = saveReComment(commentId);
        Long reCommend2Id = saveReComment(commentId);
        Long reCommend3Id = saveReComment(commentId);
        Long reCommend4Id = saveReComment(commentId);


        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getChildList().size()).isEqualTo(4);
        clear();

        commentService.remove(reCommend1Id);
        clear();

        commentService.remove(reCommend2Id);
        clear();

        commentService.remove(reCommend3Id);
        clear();

        commentService.remove(reCommend4Id);
        clear();

        assertThat(commentRepository.findById(reCommend1Id).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
        assertThat(commentRepository.findById(reCommend2Id).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
        assertThat(commentRepository.findById(reCommend3Id).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
        assertThat(commentRepository.findById(reCommend4Id).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
        clear();


        //when
        commentService.remove(commentId);
        clear();


        //then
        LongStream.rangeClosed(commentId, reCommend4Id).forEach(id ->
                assertThat(assertThrows(Exception.class, () -> commentRepository.findById(id).orElseThrow(()->
                        new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))))
        );

    }





    // 대댓글을 삭제하는 경우
    // 부모 댓글이 삭제되지 않은 경우
    // 내용만 삭제, DB에서는 삭제 X
    @Test
    public void 대댓글삭제_부모댓글이_남아있는_경우() throws Exception {
        //given
        Long commentId = saveComment();
        Long reCommend1Id = saveReComment(commentId);


        //when
        commentService.remove(reCommend1Id);
        clear();


        //then
        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
        assertThat(commentRepository.findById(reCommend1Id).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isFalse();
        assertThat(commentRepository.findById(reCommend1Id).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
    }



    // 대댓글을 삭제하는 경우
    // 부모 댓글이 삭제되어있고, 대댓글들도 모두 삭제된 경우
    // 부모를 포함한 모든 대댓글을 DB에서 일괄 삭제, 화면상에서도 지움
    @Test
    public void 대댓글삭제_부모댓글이_삭제된_경우_모든_대댓글이_삭제된_경우() throws Exception {
        //given
        Long commentId = saveComment();
        Long reCommend1Id = saveReComment(commentId);
        Long reCommend2Id = saveReComment(commentId);
        Long reCommend3Id = saveReComment(commentId);


        commentService.remove(reCommend2Id);
        clear();
        commentService.remove(commentId);
        clear();
        commentService.remove(reCommend3Id);
        clear();


        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getChildList().size()).isEqualTo(3);

        //when
        commentService.remove(reCommend1Id);



        //then
        LongStream.rangeClosed(commentId, reCommend3Id).forEach(id ->
                assertThat(assertThrows(Exception.class, () -> commentRepository.findById(id).orElseThrow(()->
                        new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))))
        );



    }


    // 대댓글을 삭제하는 경우
    // 부모 댓글이 삭제되어있고, 다른 대댓글이 아직 삭제되지 않고 남아있는 경우
    //해당 대댓글만 삭제, 그러나 DB에서 삭제되지는 않고, 화면상에는 "삭제된 댓글입니다"라고 표시
    @Test
    public void 대댓글삭제_부모댓글이_삭제된_경우_다른_대댓글이_남아있는_경우() throws Exception {
        //given
        Long commentId = saveComment();
        Long reCommend1Id = saveReComment(commentId);
        Long reCommend2Id = saveReComment(commentId);
        Long reCommend3Id = saveReComment(commentId);


        commentService.remove(reCommend3Id);
        commentService.remove(commentId);
        clear();

        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getChildList().size()).isEqualTo(3);


        //when
        commentService.remove(reCommend2Id);
        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();


        //then
        assertThat(commentRepository.findById(reCommend2Id).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
        assertThat(commentRepository.findById(reCommend2Id).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
        assertThat(commentRepository.findById(reCommend1Id).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getId()).isNotNull();
        assertThat(commentRepository.findById(reCommend3Id).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getId()).isNotNull();
        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getId()).isNotNull();

    }

    @Test
    public void 댓글저장_성공() throws Exception{
        // given
        Long boardId = saveBoard();
        CommentSaveDto commentSaveDto = new CommentSaveDto("댓글");

        // when
        commentService.save(boardId, commentSaveDto);
        clear();

        //then
        List<CommentEntity> resultList =
                em.createQuery("select c from CommentEntity c order by c.createdDate desc ", CommentEntity.class)
                        .getResultList();
        assertThat(resultList.size()).isEqualTo(1);
    }

    @Test
    public void 대댓글저장_성공() throws Exception{
        // given
        Long boardId = saveBoard();
        Long parentId = saveComment();
        CommentSaveDto commentSaveDto = new CommentSaveDto("대댓글");

        // when
        commentService.saveReComment(boardId, parentId, commentSaveDto);
        clear();

        // then
        List<CommentEntity> resultList =
                em.createQuery("select c from CommentEntity c order by c.createdDate desc ", CommentEntity.class)
                        .getResultList();
        assertThat(resultList.size()).isEqualTo(2);
    }

    @Test
    public void 댓글저장_실패_게시물이_없음() throws Exception{
        // given
        Long boardId = saveBoard();
        CommentSaveDto commentSaveDto = new CommentSaveDto("댓글");

        // when, then

        assertThat(assertThrows(BoardException.class, () ->
                commentService.save(boardId+1, commentSaveDto))
                .getExceptionType()).isEqualTo(BoardExceptionType.BOARD_NOT_FOUND);
    }

    @Test
    public void 대댓글저장_실패_게시물이_없음() throws Exception{
        // given
        Long boardId = saveBoard();
        Long parentId = saveComment();
        CommentSaveDto commentSaveDto = new CommentSaveDto("대댓글");

        // when, then
        assertThat(assertThrows(BoardException.class, () ->
                commentService.saveReComment(boardId+123, parentId, commentSaveDto))
                .getExceptionType()).isEqualTo(BoardExceptionType.BOARD_NOT_FOUND);
    }

    @Test
    public void 대댓글저장_실패_댓글이_없음() throws Exception{
        // given
        Long postId = saveBoard();
        Long parentId = saveComment();
        CommentSaveDto commentSaveDto = new CommentSaveDto("대댓글");

        //when, then
        assertThat(assertThrows(CommentException.class, () ->
                commentService.saveReComment(postId, parentId+1, commentSaveDto))
                .getExceptionType()).isEqualTo(CommentExceptionType.NOT_FOUND_COMMENT);
    }

    @Test
    public void 업데이트_성공() throws Exception{
        // given
        Long boardId = saveBoard();
        Long parentId = saveComment();
        Long reCommentId = saveReComment(parentId);
        clear();

        // when
        commentService.update(reCommentId, new CommentUpdateDto(Optional.ofNullable("업데이트")));
        clear();

        // then
        CommentEntity comment = commentRepository.findById(reCommentId).orElse(null);
        assertThat(comment.getContent()).isEqualTo("업데이트");
    }

    @Test
    public void 업데이트_실패_권한이_없음() throws Exception{
        // given
        Long boardId = saveBoard();
        Long parentId = saveComment();
        Long reCommentId = saveReComment(parentId);
        clear();

        anotherSignUpAndSetAuthentication();

        // when, then
        BaseExceptionType type = assertThrows(CommentException.class, () ->
                commentService.update(reCommentId, new CommentUpdateDto(Optional.ofNullable("업데이트"))))
                .getExceptionType();
        assertThat(type).isEqualTo(CommentExceptionType.NOT_AUTHORITY_UPDATE_COMMENT);
    }

    @Test
    public void 댓글삭제_실패_권한이_없음() throws Exception{
        //given
        Long boardId = saveBoard();
        Long parentId = saveComment();
        Long reCommentId = saveReComment(parentId);
        clear();

        anotherSignUpAndSetAuthentication();

        //when, then
        BaseExceptionType type = assertThrows(CommentException.class, () ->
                commentService.remove(reCommentId)).getExceptionType();
        assertThat(type).isEqualTo(CommentExceptionType.NOT_AUTHORITY_DELETE_COMMENT);
    }
}
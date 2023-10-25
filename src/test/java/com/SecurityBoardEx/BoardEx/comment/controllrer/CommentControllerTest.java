package com.SecurityBoardEx.BoardEx.comment.controllrer;

import com.SecurityBoardEx.BoardEx.board.dto.BoardSaveDto;
import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import com.SecurityBoardEx.BoardEx.board.repository.BoardRepository;
import com.SecurityBoardEx.BoardEx.comment.dto.CommentSaveDto;
import com.SecurityBoardEx.BoardEx.comment.entity.CommentEntity;
import com.SecurityBoardEx.BoardEx.comment.exception.CommentException;
import com.SecurityBoardEx.BoardEx.comment.exception.CommentExceptionType;
import com.SecurityBoardEx.BoardEx.comment.repository.CommentRepository;
import com.SecurityBoardEx.BoardEx.comment.service.CommentService;
import com.SecurityBoardEx.BoardEx.login.auth.service.JwtService;
import com.SecurityBoardEx.BoardEx.login.entity.Role;
import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import com.SecurityBoardEx.BoardEx.login.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;
    @PersistenceContext
    EntityManager em;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BoardRepository boardRepository;
    @Autowired
    CommentService commentService;
    @Autowired
    CommentRepository commentRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    JwtService jwtService;

    final String USERNAME = "username0";

    private static UserEntity user;

    @BeforeEach
    private void signUpAndSetAuthentication() throws Exception{
        user = userRepository.save(UserEntity.builder()
                .username(USERNAME)
                .password("!1231231")
                .nickname("USER")
                .role(Role.USER)
                .build());

        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        emptyContext.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        User.builder()
                                .username(USERNAME)
                                .password("!1231231")
                                .roles(Role.USER.toString())
                                .build(), null
                )
        );
        SecurityContextHolder.setContext(emptyContext);
        clear();
    }

    @AfterEach
    private void clear(){
        em.flush();
        em.clear();
    }

    private String getAccessToken(){
        return jwtService.createAccessToken(USERNAME);
    }

    private String getNoAuthAccessToken(){
        return jwtService.createAccessToken(USERNAME+12);
    }

    private Long saveBoard(){
        String title = "제목";
        String content = "내용";
        List<MultipartFile> files = null;
        BoardSaveDto boardSaveDto = new BoardSaveDto(title, content, files);

        // when
        BoardEntity save = boardRepository.save(boardSaveDto.toEntity());
        clear();
        return save.getId();
    }

    private Long saveComment(Long boardId){
        CommentSaveDto commentSaveDto = new CommentSaveDto("댓글");
        commentService.save(boardId,commentSaveDto);
        clear();

        List<CommentEntity> resultList = em.createQuery("select c from CommentEntity c order by c.createdDate desc "
                , CommentEntity.class).getResultList();
        return resultList.get(0).getId();
    }

    private Long saveReComment(Long parentId, Long boardId){
        CommentSaveDto commentSaveDto = new CommentSaveDto("대댓글");
        commentService.saveReComment(boardId, parentId, commentSaveDto);
        clear();

        List<CommentEntity> resultList = em.createQuery("select c from CommentEntity c order by c.createdDate desc ",
                CommentEntity.class).getResultList();
        return resultList.get(0).getId();
    }

    @Test
    public void 댓글저장_성공() throws Exception{
        // given
        Long boardId = saveBoard();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("content", "comment");

        // when
        mockMvc.perform(
                post("/comment/" + boardId)
                        .header("Authorization" , "Bearer " + getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .params(map))
                .andExpect(status().isCreated());

        //then
        List<CommentEntity> resultList = em.createQuery("select c from CommentEntity c order by c.createdDate desc ",
                CommentEntity.class).getResultList();
        assertThat(resultList.size()).isEqualTo(1);
    }

    @Test
    public void 대댓글저장_성공() throws Exception{
        // given
        Long boardId = saveBoard();
        Long parentId = saveComment(boardId);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("content", "recomment");

        // when
        mockMvc.perform(
                post("/comment/" +boardId+"/"+parentId)
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .params(map))
                .andExpect(status().isCreated());

        // then
        List<CommentEntity> resultList = em.createQuery("select c from CommentEntity c order by c.createdDate desc ",
                CommentEntity.class).getResultList();
        assertThat(resultList.size()).isEqualTo(2);
    }

    @Test
    public void 댓글저장_실패_게시물이_없음() throws Exception{
        // given
        Long boardId = saveBoard();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("content", "comment");

        // when, then
        mockMvc.perform(
                post("/comment/"+10000000)
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .params(map))
                .andExpect(status().isNotFound());
    }

    @Test
    public void 대댓글저장_실패_게시물이_없음() throws Exception{
        // given
        Long boardId = saveBoard();
        Long parentId = saveComment(boardId);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("content", "recomment");

        // when,then
        mockMvc.perform(
                        post("/comment/"+10000000 +"/"+parentId)
                                .header("Authorization", "Bearer " + getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .params(map))
                .andExpect(status().isNotFound());
    }

    @Test
    public void 대댓글저장_실패_댓글이_없음() throws Exception{
        // given
        Long boardId = saveBoard();
        Long parentId = saveComment(boardId);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("content" , "recomment");

        // when, then
        mockMvc.perform(
                post("/comment/"+boardId+"/"+10000)
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .params(map))
                .andExpect(status().isNotFound());
    }

    @Test
    public void 업데이트_성공() throws Exception{
        // given
        Long boardId = saveBoard();
        Long commentId = saveComment(boardId);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("content", "updateComment");

        // when
        mockMvc.perform(
                put("/comment/"+commentId)
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .params(map))
                .andExpect(status().isOk());

        CommentEntity comment = commentRepository.findById(commentId).orElse(null);
        assertThat(comment.getContent()).isEqualTo("updateComment");
    }

    @Test
    public void 업데이트_실패_권한이_없음() throws Exception {
        // given
        Long boardId = saveBoard();
        Long commentId = saveComment(boardId);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("content", "updateComment");

        // when
        mockMvc.perform(
                put("/comment/" + commentId)
                        .header("Authorization", "Bearer " + getNoAuthAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .params(map))
                .andExpect(status().isForbidden());

        CommentEntity comment = commentRepository.findById(commentId).orElse(null);
        assertThat(comment.getContent()).isEqualTo("댓글");
    }

    @Test
    public void 댓글삭제_실패_권한이_없음() throws Exception{
        // given
        Long boardId = saveBoard();
        Long commentId = saveComment(boardId);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("content", "updateComment");

        // when
        mockMvc.perform(
                delete("/commnet/"+commentId)
                        .header("Authorization", "Bearer "+getNoAuthAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .params(map))
                .andExpect(status().isForbidden());

        CommentEntity comment = commentRepository.findById(commentId).orElse(null);
        assertThat(comment.getContent()).isEqualTo("댓글");
    }

    /** 댓글을 삭제하는 경우
     *  1. 댓글을 삭제하는 경우 (대댓글이 없을 때(있다가 삭제 포함) or 있을 떄 )
     *  2. 대댓글을 삭제하는 경우 (댓글이 있을 떄 or 없을 때(대댓글이 있을 때 없을 때))
     */

    @Test
    public void 댓글삭제_대댓글이_남아있는_경우() throws Exception{
        // given
        Long boardId = saveBoard();
        Long commentId = saveComment(boardId);
        saveReComment(commentId, boardId);
        saveReComment(commentId, boardId);
        saveReComment(commentId, boardId);
        saveReComment(commentId, boardId);

        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))
                .getChildList().size()).isEqualTo(4);

        // when
        mockMvc.perform(
                delete("/comment/" + commentId)
                        .header("Authorization", "Bearer " + getAccessToken()))
                .andExpect(status().isOk());

        // then
        CommentEntity findComment = commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT));
        assertThat(findComment).isNotNull();
        assertThat(findComment.isRemoved()).isTrue();
        assertThat(findComment.getChildList().size()).isEqualTo(4);
    }

    @Test
    public void 댓글삭제_대댓글이_없는_경우() throws Exception{
        // given
        Long boardId = saveBoard();
        Long commentId = saveComment(boardId);

        // when
        mockMvc.perform(
                delete("/comment/"+ commentId)
                        .header("Authorization", "Bearer "+getAccessToken()))
                .andExpect(status().isOk());
        clear();

        // then
        assertThat(commentRepository.findAll().size()).isSameAs(0);
        assertThat(assertThrows(CommentException.class, ()->
                commentRepository.findById(commentId).orElseThrow(()->
                        new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)))
                .getExceptionType()).isEqualTo(CommentExceptionType.NOT_FOUND_COMMENT);
    }

    @Test
    public void 댓글삭제_대댓글이_존재하나_모두_삭제된_대댓글인_경우() throws Exception{
        // given
        Long boardId = saveBoard();
        Long commentId = saveComment(boardId);
        Long reCommentId1 = saveReComment(commentId, boardId);
        Long reCommentId2 = saveReComment(commentId, boardId);
        Long reCommentId3 = saveReComment(commentId, boardId);
        Long reCommentId4 = saveReComment(commentId, boardId);

        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))
                .getChildList().size()).isEqualTo(4);
        clear();

        commentService.remove(reCommentId1);
        clear();
        commentService.remove(reCommentId2);
        clear();
        commentService.remove(reCommentId3);
        clear();
        commentService.remove(reCommentId4);
        clear();

        assertThat(commentRepository.findById(reCommentId1).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))
                .isRemoved()).isTrue();
        assertThat(commentRepository.findById(reCommentId2).orElseThrow(()->
                        new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))
                .isRemoved()).isTrue();
        assertThat(commentRepository.findById(reCommentId3).orElseThrow(()->
                        new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))
                .isRemoved()).isTrue();
        assertThat(commentRepository.findById(reCommentId4).orElseThrow(()->
                        new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))
                .isRemoved()).isTrue();

        // when
        mockMvc.perform(
                delete("/comment/"+ commentId)
                        .header("Authorization", "Bearer " + getAccessToken()))
                .andExpect(status().isOk());
        clear();

        // then
        LongStream.rangeClosed(commentId, reCommentId4).forEach(id->
                assertThat(assertThrows(CommentException.class, ()->
                        commentRepository.findById(id).orElseThrow(()->
                                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)))
                        .getExceptionType()).isEqualTo(CommentExceptionType.NOT_FOUND_COMMENT));
    }

    @Test
    public void 대댓글삭제_부모댓글이_남아있는_경우() throws Exception{
        // given
        Long boardId = saveBoard();
        Long commentId = saveComment(boardId);
        Long reCommentId = saveReComment(commentId, boardId);

        // when
        mockMvc.perform(
                delete("/comment/"+ reCommentId)
                        .header("Authorization" , "Bearer " + getAccessToken()))
                .andExpect(status().isOk());

        // then
        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
        assertThat(commentRepository.findById(reCommentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNull();
        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isFalse();
        assertThat(commentRepository.findById(reCommentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
    }

    @Test
    public void 대댓글삭제_부모댓글과대댓글이_모두_삭제된_경우() throws Exception{
        // given
        Long boardId = saveBoard();
        Long commentId = saveComment(boardId);
        Long reCommentId1 = saveReComment(commentId, boardId);
        Long reCommentId2 = saveReComment(commentId, boardId);
        Long reCommentId3 = saveReComment(commentId, boardId);

        commentService.remove(reCommentId3);
        clear();
        commentService.remove(commentId);
        clear();
        commentService.remove(reCommentId2);
        clear();

        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getChildList().size()).isEqualTo(3);

        // when
        mockMvc.perform(
                delete("/comment/"+ reCommentId1)
                        .header("Authorization", "Bearer " + getAccessToken()))
                .andExpect(status().isOk());

        // then
        LongStream.rangeClosed(commentId, reCommentId3).forEach(id->
                assertThat(assertThrows(CommentException.class, ()->
                        commentRepository.findById(id).orElseThrow(()->
                                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)))
                        .getExceptionType()).isEqualTo(CommentExceptionType.NOT_FOUND_COMMENT));
    }

    @Test
    public void 대댓글삭제_부모댓글이_삭제되었지만_대댓글이_남아있는_경우() throws Exception{
        // given
        Long boardId = saveBoard();
        Long commentId = saveComment(boardId);
        Long reCommentId1 = saveReComment(commentId,boardId);
        Long reCommentId2 = saveReComment(commentId,boardId);
        Long reCommentId3 = saveReComment(commentId,boardId);

        commentService.remove(commentId);
        commentService.remove(reCommentId3);
        clear();

        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getChildList().size()).isEqualTo(3);

        // when
        mockMvc.perform(
                delete("/comment/"+ reCommentId2)
                        .header("Authorization", "Bearer "+getAccessToken()))
                .andExpect(status().isOk());

        assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();

        // then
        Assertions.assertThat(commentRepository.findById(reCommentId2).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
        Assertions.assertThat(commentRepository.findById(reCommentId2).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
        Assertions.assertThat(commentRepository.findById(reCommentId1).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getId()).isNotNull();
        Assertions.assertThat(commentRepository.findById(reCommentId3).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getId()).isNotNull();
        Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getId()).isNotNull();
    }
}
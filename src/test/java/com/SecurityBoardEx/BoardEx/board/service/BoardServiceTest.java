//package com.SecurityBoardEx.BoardEx.board.service;
//
//import com.SecurityBoardEx.BoardEx.board.condition.BoardSearchCondition;
//import com.SecurityBoardEx.BoardEx.board.dto.BoardInfoDto;
//import com.SecurityBoardEx.BoardEx.board.dto.BoardPagingDto;
//import com.SecurityBoardEx.BoardEx.board.dto.BoardSaveDto;
//import com.SecurityBoardEx.BoardEx.board.dto.BoardUpdateDto;
//import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
//import com.SecurityBoardEx.BoardEx.board.exception.BoardException;
//import com.SecurityBoardEx.BoardEx.board.repository.BoardRepository;
//import com.SecurityBoardEx.BoardEx.comment.dto.CommentInfoDto;
//import com.SecurityBoardEx.BoardEx.comment.entity.CommentEntity;
//import com.SecurityBoardEx.BoardEx.comment.repository.CommentRepository;
//import com.SecurityBoardEx.BoardEx.login.dto.UserSignUpDto;
//import com.SecurityBoardEx.BoardEx.login.entity.Role;
//import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
//import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
//import com.SecurityBoardEx.BoardEx.login.service.UserService;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//@SpringBootTest
//@Transactional
//class BoardServiceTest {
//
//    @PersistenceContext
//    private EntityManager em;
//
//    @Autowired
//    private BoardService boardService;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private BoardRepository boardRepository;
//    @Autowired
//    private CommentRepository commentRepository;
//
//    private static final String USERNAME = "username";
//    private static final String PASSWORD = "PASSWORD123@@@";
//
//    private void clear(){
//        em.flush();
//        em.clear();
//    }
//
//    private void deleteFile(String filePath){
//        File files = new File(filePath);
//        files.delete();
//    }
//
//    private MockMultipartFile getMockUploadFile() throws IOException{
//        return new MockMultipartFile("file", "file.png", "image/png",
//                new FileInputStream("C:/Users/dkrha/Desktop/diary.png"));
//    }
//
//    @BeforeEach
//    private void signUpAndSetAuthentication() throws Exception{
//        userService.signUp(new UserSignUpDto(USERNAME, PASSWORD, "name"));
//        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
//        emptyContext.setAuthentication(
//                new UsernamePasswordAuthenticationToken(
//                        User.builder()
//                                .username(USERNAME)
//                                .password(PASSWORD)
//                                .roles(Role.USER.toString())
//                                .build(),
//                        null
//                )
//        );
//        SecurityContextHolder.setContext(emptyContext);
//        clear();
//    }
//
//    @Test
//    public void 포스트_저장_성공_업로드_파일_없음() throws Exception{
//        // given
//        String title = "제목";
//        String content = "내용";
//        BoardSaveDto boardSaveDto = new BoardSaveDto(title, content, Optional.empty());
//
//        // when
//        boardService.save(boardSaveDto);
//        clear();
//
//        // then
//        BoardEntity findBoard = em.createQuery("select b from BoardEntity b", BoardEntity.class).getSingleResult();
//        BoardEntity board = em.find(BoardEntity.class, findBoard.getId());
//        assertThat(board.getContent()).isEqualTo(content);
//        assertThat(board.getWriter().getUsername()).isEqualTo(USERNAME);
//        assertThat(board.getFilePath()).isNull();
//    }
//
//    @Test
//    public void 포스트_저장_성공_업로드_파일_있음() throws Exception{
//        // given
//        String title = "제목";
//        String content = "내용";
//        BoardSaveDto boardSaveDto = new BoardSaveDto(title, content, Optional.ofNullable(getMockUploadFile()));
//
//        // when
//        boardService.save(boardSaveDto);
//        clear();
//
//        // then
//        BoardEntity findBoard = em.createQuery("select b from BoardEntity b", BoardEntity.class).getSingleResult();
//        BoardEntity board = em.find(BoardEntity.class, findBoard.getId());
//        assertThat(board.getContent()).isEqualTo(content);
//        assertThat(board.getWriter().getUsername()).isEqualTo(USERNAME);
//        assertThat(board.getFilePath()).isNotNull();
//
//        deleteFile(board.getFilePath());
//    }
//
//    @Test
//    public void 포스트_저장_실패_제목이나_내용이_없음() throws Exception{
//        // given
//        String title = "제목";
//        String content = "내용";
//
//        BoardSaveDto boardSaveDto = new BoardSaveDto(null, content, Optional.empty());
//        BoardSaveDto boardSaveDto2 = new BoardSaveDto(title, null, Optional.empty());
//
//        // then, when
//        assertThrows(Exception.class, () -> boardService.save(boardSaveDto));
//        assertThrows(Exception.class, () -> boardService.save(boardSaveDto2));
//    }
//
//    @Test
//    public void 포스트_업데이트_성공_업로드파일_없음TO없음() throws Exception{
//        // given
//        String title = "제목";
//        String content = "내용";
//        BoardSaveDto boardSaveDto = new BoardSaveDto(title, content, Optional.empty());
//        boardService.save(boardSaveDto);
//        clear();
//
//        //when
//        BoardEntity findBoard = em.createQuery("select b from BoardEntity  b", BoardEntity.class).getSingleResult();
//        BoardUpdateDto boardUpdateDto = new BoardUpdateDto(Optional.ofNullable("바꾼제목"),
//                Optional.ofNullable("바꾼내용"),
//                Optional.empty());
//        boardService.updated(findBoard.getId(), boardUpdateDto);
//        clear();
//
//        //then
//        BoardEntity board = em.find(BoardEntity.class, findBoard.getId());
//        assertThat(board.getContent()).isEqualTo("바꾼내용");
//        assertThat(board.getWriter().getUsername()).isEqualTo(USERNAME);
//        assertThat(board.getFilePath()).isNull();
//    }
//
//    @Test
//    public void 포스트_업데이트_성공_업로드파일_없음TO있음() throws Exception{
//        // given
//        String title = "제목";
//        String content = "내용";
//        BoardSaveDto boardSaveDto = new BoardSaveDto(title, content, Optional.empty());
//        boardService.save(boardSaveDto);
//        clear();
//
//        //when
//        BoardEntity findBoard = em.createQuery("select b from BoardEntity  b", BoardEntity.class).getSingleResult();
//        BoardUpdateDto boardUpdateDto = new BoardUpdateDto(Optional.ofNullable("바꾼제목"),
//                Optional.ofNullable("바꾼내용"),
//                Optional.ofNullable(getMockUploadFile()));
//        boardService.updated(findBoard.getId(), boardUpdateDto);
//        clear();
//
//        //then
//        BoardEntity board = em.find(BoardEntity.class, findBoard.getId());
//        assertThat(board.getContent()).isEqualTo("바꾼내용");
//        assertThat(board.getWriter().getUsername()).isEqualTo(USERNAME);
//        assertThat(board.getFilePath()).isNotNull();
//
//        deleteFile(board.getFilePath());
//    }
//
//
//    @Test
//    public void 포스트_업데이트_성공_업로드파일_있음TO없음() throws Exception{
//        // given
//        String title = "제목";
//        String content = "내용";
//        BoardSaveDto boardSaveDto = new BoardSaveDto(title, content,
//                Optional.ofNullable(getMockUploadFile()));
//        boardService.save(boardSaveDto);
//
//        BoardEntity findBoard = em.createQuery("select b from BoardEntity b", BoardEntity.class).getSingleResult();
//        assertThat(findBoard.getFilePath()).isNotNull();
//        clear();
//
//        //when
//        BoardUpdateDto boardUpdateDto = new BoardUpdateDto(Optional.ofNullable("바꾼제목"),
//                Optional.ofNullable("바꾼내용"),
//                Optional.empty());
//        boardService.updated(findBoard.getId(), boardUpdateDto);
//        clear();
//
//        //then
//        BoardEntity board = em.find(BoardEntity.class, findBoard.getId());
//        assertThat(board.getContent()).isEqualTo("바꾼내용");
//        assertThat(board.getWriter().getUsername()).isEqualTo(USERNAME);
//        assertThat(board.getFilePath()).isNull();
//    }
//
//    @Test
//    public void 포스트_업데이트_성공_업로드파일_있음TO있음() throws Exception{
//        // given
//        String title = "제목";
//        String content = "내용";
//        BoardSaveDto boardSaveDto = new BoardSaveDto(title, content,
//                Optional.ofNullable(getMockUploadFile()));
//        boardService.save(boardSaveDto);
//
//        BoardEntity findBoard = em.createQuery("select b from BoardEntity  b", BoardEntity.class).getSingleResult();
//        BoardEntity board = em.find(BoardEntity.class, findBoard.getId());
//        String filePath = board.getFilePath();
//
//        clear();
//
//        //when
//        BoardUpdateDto boardUpdateDto = new BoardUpdateDto(Optional.ofNullable("바꾼제목"),
//                Optional.ofNullable("바꾼내용"),
//                Optional.ofNullable(getMockUploadFile()));
//        boardService.updated(findBoard.getId(), boardUpdateDto);
//        clear();
//
//        //then
//        board = em.find(BoardEntity.class, findBoard.getId());
//        assertThat(board.getContent()).isEqualTo("바꾼내용");
//        assertThat(board.getWriter().getUsername()).isEqualTo(USERNAME);
//        assertThat(board.getFilePath()).isNotEqualTo(filePath);
//
//        deleteFile(board.getFilePath());
//    }
//
//    private void setAnotherAuthentication() throws Exception{
//        userService.signUp(new UserSignUpDto(USERNAME+"123",
//                PASSWORD,"name"));
//        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
//        emptyContext.setAuthentication(
//                new UsernamePasswordAuthenticationToken(
//                        User.builder()
//                                .username(USERNAME+"123")
//                                .password(PASSWORD)
//                                .roles(Role.USER.toString())
//                                .build(),
//                        null)
//        );
//        SecurityContextHolder.setContext(emptyContext);
//        clear();
//    }
//
//    @Test
//    public void 포스트_업데이트_실패_권한이없음() throws Exception{
//        // given
//        String title = "제목";
//        String content = "내용";
//        BoardSaveDto boardSaveDto = new BoardSaveDto(title, content, Optional.empty());
//
//        boardService.save(boardSaveDto);
//        clear();
//
//        // when, then
//        setAnotherAuthentication();
//        BoardEntity findBoard = em.createQuery("select b from BoardEntity b", BoardEntity.class).getSingleResult();
//        BoardUpdateDto boardUpdateDto = new BoardUpdateDto(Optional.ofNullable("바꾼제목"),
//                Optional.ofNullable("바꾼 내용"),
//                Optional.empty());
//
//        assertThrows(BoardException.class, () -> boardService.updated(findBoard.getId(),boardUpdateDto));
//    }
//
//    @Test
//    public void 포스트_삭제_성공() throws Exception{
//        // given
//        String title = "제목";
//        String content = "내용";
//        BoardSaveDto boardSaveDto = new BoardSaveDto(title, content, Optional.empty());
//        boardService.save(boardSaveDto);
//        clear();
//        // when
//        BoardEntity findBoard = em.createQuery("select b from BoardEntity  b", BoardEntity.class).getSingleResult();
//        boardService.delete(findBoard.getId());
//        //then
//        List<BoardEntity> findBoards = em.createQuery("select b from BoardEntity  b", BoardEntity.class).getResultList();
//        assertThat(findBoards.size()).isEqualTo(0);
//    }
//
//    @Test
//    public void 포스트_삭제_실패() throws Exception{
//        // given
//        String title = "제목";
//        String content = "내용";
//        BoardSaveDto boardSaveDto = new BoardSaveDto(title, content, Optional.empty());
//
//        boardService.save(boardSaveDto);
//        clear();
//
//        // when, then
//        setAnotherAuthentication();
//        BoardEntity findBoard = em.createQuery("select b from BoardEntity b", BoardEntity.class).getSingleResult();
//        assertThrows(BoardException.class, () -> boardService.delete(findBoard.getId()));
//    }
//
//
//
//    @Test
//    public void 포스트_조회() throws Exception {
//        UserEntity member1 = userRepository.save(UserEntity.builder().username("username1").password("1234567890").nickname("USER1").role(Role.USER).build());
//        UserEntity member2 = userRepository.save(UserEntity.builder().username("username2").password("1234567890").nickname("USER1").role(Role.USER).build());
//        UserEntity member3 = userRepository.save(UserEntity.builder().username("username3").password("1234567890").nickname("USER1").role(Role.USER).build());
//        UserEntity member4 = userRepository.save(UserEntity.builder().username("username4").password("1234567890").nickname("USER1").role(Role.USER).build());
//        UserEntity member5 = userRepository.save(UserEntity.builder().username("username5").password("1234567890").nickname("USER1").role(Role.USER).build());
//
//        Map<Integer, Long> memberIdMap = new HashMap<>();
//        memberIdMap.put(1,member1.getId());
//        memberIdMap.put(2,member2.getId());
//        memberIdMap.put(3,member3.getId());
//        memberIdMap.put(4,member4.getId());
//        memberIdMap.put(5,member5.getId());
//
//
//
//        /**
//         * Post 생성
//         */
//
//        BoardEntity board = BoardEntity.builder().title("게시글").content("내용").build();
//        board.confirmWriter(member1);
//        boardRepository.save(board);
//        em.flush();
//
//
//        /**
//         * Comment 생성(댓글)
//         */
//
//        final int COMMENT_COUNT = 10;
//
//        for(int i = 1; i<=COMMENT_COUNT; i++ ){
//            CommentEntity comment = CommentEntity.builder().content("댓글" + i).build();
//            comment.confirmWriter(userRepository.findById(memberIdMap.get(i % 3 + 1)).orElse(null));
//            comment.confirmBoard(board);
//            commentRepository.save(comment);
//        }
//
//
//
//
//
//
//        /**
//         * ReComment 생성(대댓글)
//         */
//        final int COMMENT_PER_RECOMMENT_COUNT = 20;
//        commentRepository.findAll().stream().forEach(comment -> {
//
//            for(int i = 1; i<=20; i++ ){
//                CommentEntity recomment = CommentEntity.builder().build().builder().content("대댓글" + i).build();
//                recomment.confirmWriter(userRepository.findById(memberIdMap.get(i % 3 + 1)).orElse(null));
//
//                recomment.confirmBoard(comment.getBoard());
//                recomment.confirmParent(comment);
//                commentRepository.save(recomment);
//            }
//        });
//
//        clear();
//
//        //when
//        BoardInfoDto boardInfo = boardService.getBoardInfo(board.getId());
//
//
//
//        //then
//        assertThat(boardInfo.getBoardId()).isEqualTo(board.getId());
//        assertThat(boardInfo.getContent()).isEqualTo(board.getContent());
//        assertThat(boardInfo.getWriterDto().getUsername()).isEqualTo(board.getWriter().getUsername());
//
//        int recommentCount = 0;
//        for (CommentInfoDto commentInfoDto : boardInfo.getCommentInfoDtoList()) {
//            recommentCount += commentInfoDto.getReCommentInfoDtoList().size();
//        }
//
//        assertThat(boardInfo.getCommentInfoDtoList().size()).isEqualTo(COMMENT_COUNT);
//        assertThat(recommentCount).isEqualTo(COMMENT_PER_RECOMMENT_COUNT * COMMENT_COUNT);
//
//    }
//
//    /** 게시글 검색 테스트 코드 작성 **/
//     @Test
//    public void 포스트_검색_조건_없음() throws Exception{
//         // given
//         UserEntity user1 = userRepository.save(UserEntity.builder()
//                 .username("username0")
//                 .password("1234567890")
//                 .nickname("USER1")
//                 .role(Role.USER)
//                 .build());
//
//         final int POST_COUNT = 50;
//         for(int i = 1; i <= POST_COUNT; i++){
//             BoardEntity board = BoardEntity.builder()
//                     .title("게시글" + i)
//                     .content("내용" + i)
//                     .build();
//             board.confirmWriter(user1);
//             boardRepository.save(board);
//         }
//
//         clear();
//
//         // when
//         final int PAGE = 0;
//         final int SIZE = 20;
//         PageRequest pageRequest = PageRequest.of(PAGE, SIZE);
//
//         BoardSearchCondition boardSearchCondition = new BoardSearchCondition();
//
//         BoardPagingDto boardList = boardService.getBoardList(pageRequest, boardSearchCondition);
//
//         // then
//         assertThat(boardList.getTotalElementCount()).isEqualTo(POST_COUNT);
//         assertThat(boardList.getTotalPageCount()).isEqualTo(((POST_COUNT) % SIZE == 0) ?(POST_COUNT)/SIZE :(POST_COUNT)/SIZE +1);
//         assertThat(boardList.getCurrentPageNum()).isEqualTo(PAGE);
//         assertThat(boardList.getCurrentPageElementCount()).isEqualTo(SIZE);
//     }
//
//    @Test
//    public void 포스트_검색_제목_일치() throws Exception {
//        // given
//        UserEntity user1 = userRepository.save(UserEntity.builder()
//                .username("username0")
//                .password("1234567890")
//                .nickname("USER1")
//                .role(Role.USER)
//                .build());
//
//        final int DEFAULT_BOARD_COUNT = 100;
//        for (int i = 1; i <= DEFAULT_BOARD_COUNT; i++) {
//            BoardEntity board = BoardEntity.builder()
//                    .title("게시글" + i)
//                    .content("내용" + i).build();
//            boardRepository.save(board);
//        }
//
//        final String SEARCH_TITLE_STR = "AAA";
//        final int COND_BOARD_COUNT = 100;
//
//        for(int i =1; i <= COND_BOARD_COUNT; i++){
//            BoardEntity board = BoardEntity.builder()
//                    .title(SEARCH_TITLE_STR)
//                    .content("내용" + i)
//                    .build();
//            board.confirmWriter(user1);
//            boardRepository.save(board);
//        }
//        clear();
//
//        // when
//        final int PAGE = 2;
//        final int SIZE = 20;
//        PageRequest pageRequest = PageRequest.of(PAGE, SIZE);
//
//        BoardSearchCondition boardSearchCondition = new BoardSearchCondition();
//        boardSearchCondition.setTitle(SEARCH_TITLE_STR);
//
//        BoardPagingDto boardList = boardService.getBoardList(pageRequest, boardSearchCondition);
//
//        // then
//        assertThat(boardList.getTotalElementCount()).isEqualTo(COND_BOARD_COUNT);
//        assertThat(boardList.getTotalPageCount()).isEqualTo(((COND_BOARD_COUNT) % SIZE == 0)
//                ? (COND_BOARD_COUNT)/SIZE
//                : (COND_BOARD_COUNT)/SIZE + 1);
//        assertThat(boardList.getCurrentPageNum()).isEqualTo(PAGE);
//        assertThat(boardList.getCurrentPageElementCount()).isEqualTo(SIZE);
//    }
//
//    @Test
//    public void 포스트_검색_제목과내용일치() throws Exception{
//        // given
//        UserEntity user1 = userRepository.save(UserEntity.builder()
//                .username("username0")
//                .password("1234567890")
//                .nickname("USER1")
//                .role(Role.USER)
//                .build());
//
//        final int DEFAULT_BOARD_COUNT = 100;
//        for (int i = 1; i <= DEFAULT_BOARD_COUNT; i++) {
//            BoardEntity board = BoardEntity.builder()
//                    .title("게시글" + i)
//                    .content("내용" + i).build();
//            boardRepository.save(board);
//        }
//
//        final String SEARCH_TITLE_STR = "AAA";
//        final String SEARCH_CONTENT_STR = "BBB";
//        final int COND_BOARD_COUNT = 100;
//
//        for(int i =1; i <= COND_BOARD_COUNT; i++){
//            BoardEntity board = BoardEntity.builder()
//                    .title(SEARCH_TITLE_STR)
//                    .content(SEARCH_CONTENT_STR)
//                    .build();
//            board.confirmWriter(user1);
//            boardRepository.save(board);
//        }
//        clear();
//
//        // when
//        final int PAGE = 2;
//        final int SIZE = 20;
//        PageRequest pageRequest = PageRequest.of(PAGE, SIZE);
//
//        BoardSearchCondition boardSearchCondition = new BoardSearchCondition();
//        boardSearchCondition.setTitle(SEARCH_TITLE_STR);
//        boardSearchCondition.setContent(SEARCH_CONTENT_STR);
//
//        BoardPagingDto boardList = boardService.getBoardList(pageRequest, boardSearchCondition);
//
//        // then
//        assertThat(boardList.getTotalElementCount()).isEqualTo(COND_BOARD_COUNT);
//        assertThat(boardList.getTotalPageCount()).isEqualTo(((COND_BOARD_COUNT) % SIZE == 0)
//                ? (COND_BOARD_COUNT)/SIZE
//                : (COND_BOARD_COUNT)/SIZE + 1);
//        assertThat(boardList.getCurrentPageNum()).isEqualTo(PAGE);
//        assertThat(boardList.getCurrentPageElementCount()).isEqualTo(SIZE);
//    }
//}
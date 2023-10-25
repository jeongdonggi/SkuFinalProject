package com.SecurityBoardEx.BoardEx.board.controller;

import com.SecurityBoardEx.BoardEx.board.dto.BoardInfoDto;
import com.SecurityBoardEx.BoardEx.board.dto.BoardPagingDto;
import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import com.SecurityBoardEx.BoardEx.board.repository.BoardRepository;
import com.SecurityBoardEx.BoardEx.file.service.FileService;
import com.SecurityBoardEx.BoardEx.login.auth.service.JwtService;
import com.SecurityBoardEx.BoardEx.login.entity.Role;
import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.method.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BoardControllerTest {

    @Autowired
    MockMvc mockMvc;
    @PersistenceContext
    EntityManager em;

    @Autowired
    UserRepository userRepository;
    @Autowired
    BoardRepository boardRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtService jwtService;

    final String USERNAME = "username0";

    private static UserEntity user;

    private void clear(){
        em.flush();
        em.clear();
    }

    @BeforeEach
    public void signUpUser(){
        user = userRepository.save(UserEntity.builder()
                .username(USERNAME)
                .password("1234567890")
                .nickname("USER0")
                .role(Role.USER)
                .build());
        clear();
    }

    private String getAccessToken(){
        return jwtService.createAccessToken(USERNAME);
    }

    private MockMultipartFile getMockUploadFile() throws IOException{
        return new MockMultipartFile("uploadFile", "file.png", "image/png"
                ,new FileInputStream("C:/Users/dkrha/Desktop/diary.png"));
    }

    /** 게시글 저장 **/
    @Test
    public void 게시글_저장_성공() throws Exception{
        // given
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "제목");
        map.add("content", "내용");

        // when
        mockMvc.perform(
                post("/board")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA).params(map))
                .andExpect(status().isCreated());

        // then
        assertThat(boardRepository.findAll().size()).isEqualTo(1+51);
    }

    @Test
    public void 게시글_저장_실패_제목이나_내용이_없음() throws Exception{
        // given
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "제목");

        // when
        mockMvc.perform(
                post("/board")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .params(map))
                .andExpect(status().isBadRequest());

        map = new LinkedMultiValueMap<>();
        map.add("content", "내용");
        mockMvc.perform(
                post("/board")
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .params(map))
                .andExpect(status().isBadRequest());
    }

//    @Test
//    public void 게시글_수정() throws Exception {
//        // given
//        BoardEntity board = BoardEntity.builder().title("수정전제목").content("수정전내용").build();
//        board.confirmWriter(user);
//        BoardEntity saveBoard = boardRepository.save(board);
//
//        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
//
//        final String UPDATE_TITLE = "제목";
//        map.add("title", UPDATE_TITLE);
//        map.add("boardId", saveBoard.getId().toString());
//
//        //when
//        mockMvc.perform(
//                        post("/board/update")
//                                .header("Authorization", "Bearer " + getAccessToken())
//                                .contentType(MediaType.MULTIPART_FORM_DATA)
//                                .params(map))
//                .andExpect(status().isOk());
//
//        // then
//        assertThat(boardRepository.findById(saveBoard.getId()).get().getTitle()).isEqualTo(UPDATE_TITLE);
//    }


    @Test
    public void 게시글_수정_제목변경_성공() throws Exception {
        // given
        BoardEntity board = BoardEntity.builder().title("수정전제목").content("수정전내용").build();
        board.confirmWriter(user);
        BoardEntity saveBoard = boardRepository.save(board);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        final String UPDATE_TITLE = "제목";
        map.add("title", UPDATE_TITLE);

        //when
        mockMvc.perform(
                        put("/board/" + saveBoard.getId())
                                .header("Authorization", "Bearer " + getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .params(map))
                .andExpect(status().isOk());

        // then
        assertThat(boardRepository.findAll().get(51).getTitle()).isEqualTo(UPDATE_TITLE);
    }

    @Test
    public void 게시글_수정_내용변경_성공() throws Exception {
        // given
        BoardEntity board = BoardEntity.builder().title("수정전제목").content("수정전내용").build();
        board.confirmWriter(user);
        BoardEntity saveBoard = boardRepository.save(board);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        final String UPDATE_CONTENT = "내용";
        map.add("content", UPDATE_CONTENT);

        //when
        mockMvc.perform(
                        put("/board/" + saveBoard.getId())
                                .header("Authorization", "Bearer " + getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .params(map))
                .andExpect(status().isOk());

        // then
        assertThat(boardRepository.findAll().get(51).getContent()).isEqualTo(UPDATE_CONTENT);
    }

    @Test
    public void 게시글_수정_제목내용변경_성공() throws Exception {
        // given
        BoardEntity board = BoardEntity.builder().title("수정전제목").content("수정전내용").build();
        board.confirmWriter(user);
        BoardEntity saveBoard = boardRepository.save(board);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        final String UPDATE_TITLE = "제목";
        final String UPDATE_CONTENT = "내용";
        map.add("title", UPDATE_TITLE);
        map.add("content", UPDATE_CONTENT);

        //when
        mockMvc.perform(
                        put("/board/" + saveBoard.getId())
                                .header("Authorization", "Bearer " + getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .params(map))
                .andExpect(status().isOk());

        // then
        assertThat(boardRepository.findAll().get(51).getContent()).isEqualTo(UPDATE_CONTENT);
        assertThat(boardRepository.findAll().get(51).getTitle()).isEqualTo(UPDATE_TITLE);
    }

    @Test
    public void 게시글_수정_업로드파일추가_성공() throws Exception{
        // given
        BoardEntity board = BoardEntity.builder().title("수정전제목").content("수정전내용").build();
        board.confirmWriter(user);
        BoardEntity saveBoard = boardRepository.save(board);

        MockMultipartFile mockUploadFile = getMockUploadFile();

        // when
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart("/board/"+ saveBoard.getId());
        requestBuilder.with(request -> {
            request.setMethod(HttpMethod.PUT.name());
            return request;
        });

        mockMvc.perform(requestBuilder.file(mockUploadFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer " + getAccessToken()))
                .andExpect(status().isOk());

        // then
        String filePath = boardRepository.findAll().get(51).getFilePath();
        assertThat(filePath).isNotNull();
        assertThat(new File(filePath).delete()).isTrue(); // 파일 삭제 후 성공했는지 확인
    }

    @Autowired
    private FileService fileService;

    @Test
    public void 게시글_수정_업로드파일제거_성공() throws Exception{
        // given
        BoardEntity board = BoardEntity.builder().title("수정전제목").content("수정전내용").build();
        board.confirmWriter(user);
        String path = fileService.save(getMockUploadFile());
        board.updateFilePath(path);
        BoardEntity saveBoard = boardRepository.save(board);

        assertThat(boardRepository.findAll().get(51).getFilePath()).isNotNull();

        MockMultipartFile multipartFile = getMockUploadFile();

        // when
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        final String UPDATE_CONTENT = "내용";
        final String UPDATE_TITLE = "제목";
        map.add("title", UPDATE_TITLE);
        map.add("content", UPDATE_CONTENT);

        mockMvc.perform(
                put("/board/" + saveBoard.getId())
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .params(map))
                .andExpect(status().isOk());

        // then
        assertThat(boardRepository.findAll().get(51).getContent()).isEqualTo(UPDATE_CONTENT);
        assertThat(boardRepository.findAll().get(51).getTitle()).isEqualTo(UPDATE_TITLE);
        assertThat(boardRepository.findAll().get(51).getFilePath()).isNull();
    }

    @Test
    public void 게시글_수정_실패_권한없음() throws Exception{
        // given
        UserEntity newUser = userRepository.save(UserEntity.builder()
                .username("newUser")
                .password("!23123124421")
                .nickname("USER1")
                .role(Role.USER).build());
        BoardEntity board = BoardEntity.builder().title("수정전제목").content("수정전내용").build();
        board.confirmWriter(newUser);
        BoardEntity saveBoard = boardRepository.save(board);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        final String UPDATE_CONTENT = "내용";
        final String UPDATE_TITLE = "제목";
        map.add("title", UPDATE_TITLE);
        map.add("content", UPDATE_CONTENT);

        // when
        mockMvc.perform(
                put("/board/" + saveBoard.getId())
                        .header("Authorization", "Bearer " + getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .params(map))
                .andExpect(status().isForbidden());

        assertThat(boardRepository.findAll().get(51).getContent()).isEqualTo("수정전내용");
        assertThat(boardRepository.findAll().get(51).getTitle()).isEqualTo("수정전제목");
    }

    @Test
    public void 게시글_삭제_성공() throws Exception{
        // given
        BoardEntity board = BoardEntity.builder().title("수정전제목").content("수정전내용").build();
        board.confirmWriter(user);
        BoardEntity saveBoard = boardRepository.save(board);

        // when
        mockMvc.perform(
                delete("/board/"+ saveBoard.getId())
                        .header("Authorization", "Bearer " + getAccessToken()))
                .andExpect(status().isOk());

        // then
        assertThat(boardRepository.findAll().size()).isEqualTo(51);
    }

    @Test
    public void 게시글_삭제_실패_권한없음() throws Exception{
        //given
        UserEntity newUser = userRepository.save(UserEntity.builder()
                .username("newUser")
                .password("!1231232321")
                .nickname("USER")
                .role(Role.USER).build());
        BoardEntity board = BoardEntity.builder().title("수정전제목").content("수정전내용").build();
        board.confirmWriter(newUser);
        BoardEntity saveBoard = boardRepository.save(board);

        // when
        mockMvc.perform(
                delete("/board/" + saveBoard.getId())
                        .header("Authorization", "Bearer " + getAccessToken()))
                .andExpect(status().isForbidden());

        // then
        assertThat(boardRepository.findAll().size()).isEqualTo(52);
    }

    @Test
    public void 게시글_조회() throws Exception{

        // given
        UserEntity newUser = userRepository.save(UserEntity.builder()
                .username("newUser")
                .password("!12323321")
                .nickname("NEWUSER")
                .role(Role.USER).build());

        BoardEntity board = BoardEntity.builder().title("제목").content("내용").build();
        board.confirmWriter(newUser);
        BoardEntity saveBoard = boardRepository.save(board);

        // when
        MvcResult result = mockMvc.perform(
                        get("/board/" + saveBoard.getId())
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer " + getAccessToken()))
                .andExpect(status().isOk()).andReturn();

        BoardInfoDto boardInfoDto = objectMapper.readValue(result.getResponse().getContentAsString(), BoardInfoDto.class);

        // then
        assertThat(boardInfoDto.getBoardId()).isEqualTo(board.getId());
        assertThat(boardInfoDto.getContent()).isEqualTo(board.getContent());
        assertThat(boardInfoDto.getTitle()).isEqualTo(board.getTitle());
    }

    @Value("${spring.data.web.pageable.default-page-size}")
    private int pageCount;

    private int PAGE = 0; //3페이지

    @Test
    public void 게시글_검색() throws Exception{
        // given
        UserEntity newUser = userRepository.save(UserEntity.builder()
                .username("newUser")
                .password("!1234123")
                .nickname("NEWUSER")
                .role(Role.USER).build());

        final int POST_COUNT = 50;
        for(int i = 1; i <= POST_COUNT; i++ ){
            BoardEntity board = BoardEntity.builder().title("title" + i).content("content" + i).build();
            board.confirmWriter(newUser);
            boardRepository.save(board);
        }

        clear();

        // when
        MvcResult result = mockMvc.perform(
                get("/board?page=" + PAGE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization" ,"Bearer " + getAccessToken()))
                .andExpect(status().isOk()).andReturn();

        // then
        BoardPagingDto boardList = objectMapper.readValue(result.getResponse().getContentAsString(), BoardPagingDto.class);

        assertThat(boardList.getTotalElementCount()).isEqualTo(POST_COUNT+51);
        assertThat(boardList.getCurrentPageElementCount()).isEqualTo(pageCount);
        assertThat(boardList.getSimpleLectureDtoList().get(0%(20*(PAGE == 0 ? 1 : PAGE+1))).getContent()).isEqualTo("content50");
    }
}
package com.SecurityBoardEx.BoardEx.login;

import com.SecurityBoardEx.BoardEx.login.entity.Role;
import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Copy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class LoginTest {

    private static final String LOGIN_FAIL_MESSAGE = "로그인 실패 : 이메일이나 비밀번호를 확인해주세요.";
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @PersistenceContext
    EntityManager em;

    PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    ObjectMapper objectMapper = new ObjectMapper();

    private static String KEY_USERNAME = "username";
    private static String KEY_PASSWORD = "password";
    private static String USERNAME = "username";
    private static String PASSWORD = "123456789";

    private static String LOGIN_RUL = "/login";

    @Value("${jwt.access.header}")
    private String accessHeader;
    @Value("${jwt.refresh.header}")
    private String refreshHeader;


    private void clear() {
        em.flush();
        em.clear();
    }


    @BeforeEach
    private void init() {
        userRepository.save(UserEntity.builder()
                .username(USERNAME)
                .password(delegatingPasswordEncoder.encode(PASSWORD))
                .nickname("Member1")
                .role(Role.USER)
                .build());
        clear();
    }

    private Map getUsernamePasswordMap(String username, String password) {
        Map<String, String> map = new HashMap<>();
        map.put(KEY_USERNAME, username);
        map.put(KEY_PASSWORD, password);
        return map;
    }


    private ResultActions perform(String url, MediaType mediaType, Map usernamePasswordMap) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                .post(url)
                .contentType(mediaType)
                .content(objectMapper.writeValueAsString(usernamePasswordMap)));
    }

    @Test
    public void 로그인_성공() throws Exception {
        //given
        Map<String, String> map = getUsernamePasswordMap(USERNAME, PASSWORD);


        //when, then
        MvcResult result = perform(LOGIN_RUL, APPLICATION_JSON, map)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void 로그인_실패_아이디틀림() throws Exception {
        //given
        Map<String, String> map = new HashMap<>();
        map.put("username",USERNAME+"123");
        map.put("password",PASSWORD);


        //when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(LOGIN_RUL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                //.andExpect(status().isOk())//TODO 상테코드변경
                .andExpect(status().isBadRequest())
                .andReturn();


        //then
        assertThat(result.getResponse().getHeader(accessHeader)).isNull();
        assertThat(result.getResponse().getHeader(refreshHeader)).isNull();

    }




    @Test
    public void 로그인_실패_비밀번호틀림() throws Exception {
        //given
        Map<String, String> map = new HashMap<>();
        map.put("username",USERNAME);
        map.put("password",PASSWORD+"123");


        //when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(LOGIN_RUL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                //.andExpect(status().isOk())//TODO 상태코드변경
                .andExpect(status().isBadRequest())
                .andReturn();


        //then
        assertThat(result.getResponse().getHeader(accessHeader)).isNull();
        assertThat(result.getResponse().getHeader(refreshHeader)).isNull();

    }


    @Test
    public void 로그인_주소가_틀리면_FORBIDDEN() throws Exception {
        //given
        Map<String, String> map = getUsernamePasswordMap(USERNAME, PASSWORD);


        //when, then
        perform(LOGIN_RUL+"123", APPLICATION_JSON, map)
                .andDo(print())
                .andExpect(status().isForbidden());

    }

// 로그인_데이터형식_JSON이_아니면_200
    @Test
    public void 로그인_데이터형식_JSON이_아니면_400() throws Exception {

        //given
        Map<String, String> map = new HashMap<>();
        map.put("username",USERNAME);
        map.put("password",PASSWORD);


        //when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(LOGIN_RUL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                //.andExpect(status().isOk())  TODO 상태코드변겅
                .andExpect(status().isBadRequest())
                .andReturn();

        //then
        assertThat(result.getResponse().getContentAsString()).isEqualTo(LOGIN_FAIL_MESSAGE);
    }

    @Test
    public void 로그인_HTTP_METHOD_GET이면_NOTFOUND() throws Exception {
        //given
        Map<String, String> map = getUsernamePasswordMap(USERNAME, PASSWORD);


        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .get(LOGIN_RUL)
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void 오류_로그인_HTTP_METHOD_PUT이면_NOTFOUND() throws Exception {
        //given
        Map<String, String> map = getUsernamePasswordMap(USERNAME, PASSWORD);


        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .put(LOGIN_RUL)
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
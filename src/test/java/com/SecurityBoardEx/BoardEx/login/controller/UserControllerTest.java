package com.SecurityBoardEx.BoardEx.login.controller;

import com.SecurityBoardEx.BoardEx.login.exception.UserExceptionType;
import com.SecurityBoardEx.BoardEx.login.dto.UserSignUpDto;
import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import com.SecurityBoardEx.BoardEx.login.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @PersistenceContext
    EntityManager em;
    
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    PasswordEncoder passwordEncoder;

    private static String SIGN_UP_URL = "/signUp";

    private String username = "username";
    private String password = "Password1234@";
    private String nickName = "shinD";

    private void clear(){
        em.flush();
        em.clear();
    }

    private void signUp(String signUpData) throws Exception {
        mockMvc.perform(
                        post(SIGN_UP_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(signUpData))
                .andExpect(status().isOk());
    }

    @Value("${jwt.access.header}")
    private String accessHeader;

    private static final String BEARER = "Bearer ";

    private String getAccessToken() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("username",username);
        map.put("password",password);

        MvcResult result = mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(map)))
                .andExpect(status().isOk()).andReturn();

        return result.getResponse().getHeader(accessHeader);
    }

    private void signUpFail(String signUpData) throws Exception {
        mockMvc.perform(
                        post(SIGN_UP_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(signUpData))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void 회원가입_성공() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new UserSignUpDto(username, password, nickName));

        //when
        signUp(signUpData);

        //then
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 없습니다"));
        assertThat(user.getNickname()).isEqualTo(nickName);
        assertThat(userRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    public void 회원가입_실패_필드가_없음() throws Exception {
        //given
        String noUsernameSignUpData = objectMapper.writeValueAsString(new UserSignUpDto(null, password, nickName));
        String noPasswordSignUpData = objectMapper.writeValueAsString(new UserSignUpDto(username, null, nickName));
        String noNickNameSignUpData = objectMapper.writeValueAsString(new UserSignUpDto(username, password, null));

        //when, then
        // signUp(noUsernameSignUpData);//예외가 발생하더라도 상태코드는 200
        // signUp(noPasswordSignUpData);//예외가 발생하더라도 상태코드는 200
        // signUp(noNickNameSignUpData);//예외가 발생하더라도 상태코드는 200

        signUpFail(noUsernameSignUpData);//예외가 발생하더라도 상태코드는 400
        signUpFail(noPasswordSignUpData);//예외가 발생하더라도 상태코드는 400
        signUpFail(noNickNameSignUpData);//예외가 발생하더라도 상태코드는 400

        assertThat(userRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    public void 회원정보수정_성공() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new UserSignUpDto(username, password, nickName));

        signUp(signUpData);

        String accessToken = getAccessToken();
        Map<String, Object> map = new HashMap<>();
        map.put("nickname",nickName+"변경");
        String updateMemberData = objectMapper.writeValueAsString(map);

        //when
        mockMvc.perform(
                        put("/user/name")
                                .header(accessHeader,BEARER+accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateMemberData))
                .andExpect(status().isOk());

        //then
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 없습니다"));
        assertThat(user.getNickname()).isEqualTo(nickName+"변경");
        assertThat(userRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    public void 비밀번호수정_성공() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new UserSignUpDto(username, password, nickName));
        signUp(signUpData);

        String accessToken = getAccessToken();

        Map<String, Object> map = new HashMap<>();
        map.put("checkPassword",password);
        map.put("toBePassword",password+"!@#@!#@!#");

        String updatePassword = objectMapper.writeValueAsString(map);

        //when
        mockMvc.perform(
                        put("/user/password")
                                .header(accessHeader,BEARER+accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePassword))
                .andExpect(status().isOk());

        //then
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 없습니다"));
        assertThat(passwordEncoder.matches(password, user.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(password+"!@#@!#@!#", user.getPassword())).isTrue();
    }

    @Test
    public void 비밀번호수정_실패_검증비밀번호가_틀림() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new UserSignUpDto(username, password, nickName));
        signUp(signUpData);

        String accessToken = getAccessToken();

        Map<String, Object> map = new HashMap<>();
        map.put("checkPassword",password+"1");
        map.put("toBePassword",password+"!@#@!#@!#");

        String updatePassword = objectMapper.writeValueAsString(map);

        //when
        mockMvc.perform(
                        put("/user/password")
                                .header(accessHeader,BEARER+accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePassword))
                .andExpect(status().isBadRequest());

        //then
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 없습니다"));
        assertThat(passwordEncoder.matches(password, user.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(password+"!@#@!#@!#", user.getPassword())).isFalse();
    }

    @Test
    public void 비밀번호수정_실패_바꾸려는_비밀번호_형식_올바르지않음() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new UserSignUpDto(username, password, nickName));
        signUp(signUpData);

        String accessToken = getAccessToken();

        Map<String, Object> map = new HashMap<>();
        map.put("checkPassword",password);
        map.put("toBePassword","123123");

        String updatePassword = objectMapper.writeValueAsString(map);

        //when
        mockMvc.perform(
                        put("/user/password")
                                .header(accessHeader,BEARER+accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePassword))
                .andExpect(status().isBadRequest());

        //then
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 없습니다"));
        assertThat(passwordEncoder.matches(password, user.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("123123", user.getPassword())).isFalse();
    }

    @Test
    public void 회원탈퇴_성공() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new UserSignUpDto(username, password, nickName));
        signUp(signUpData);

        String accessToken = getAccessToken();

        Map<String, Object> map = new HashMap<>();
        map.put("checkPassword",password);

        String updatePassword = objectMapper.writeValueAsString(map);

        //when
        mockMvc.perform(
                        delete("/user")
                                .header(accessHeader,BEARER+accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePassword))
                .andExpect(status().isOk());

        //then
        assertThrows(Exception.class, () -> userRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 없습니다")));
    }

    @Test
    public void 회원탈퇴_실패_비밀번호틀림() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new UserSignUpDto(username, password, nickName));
        signUp(signUpData);

        String accessToken = getAccessToken();

        Map<String, Object> map = new HashMap<>();
        map.put("checkPassword",password+11);

        String updatePassword = objectMapper.writeValueAsString(map);

        //when
        mockMvc.perform(
                        delete("/user")
                                .header(accessHeader,BEARER+accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePassword))
                .andExpect(status().isBadRequest());

        //then
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 없습니다"));
        assertThat(user).isNotNull();
    }

    @Test
    public void 회원탈퇴_실패_권한이없음() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new UserSignUpDto(username, password, nickName));
        signUp(signUpData);

        String accessToken = getAccessToken();

        Map<String, Object> map = new HashMap<>();
        map.put("checkPassword",password);

        String updatePassword = objectMapper.writeValueAsString(map);

        //when
        mockMvc.perform(
                        delete("/user")
                                .header(accessHeader,BEARER+accessToken+"1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePassword))
                .andExpect(status().isForbidden());

        //then
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 없습니다"));
        assertThat(user).isNotNull();
    }

    @Test
    public void 내정보조회_성공() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new UserSignUpDto(username, password, nickName));
        signUp(signUpData);

        String accessToken = getAccessToken();

        //when
        MvcResult result = mockMvc.perform(
                        get("/user")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(accessHeader, BEARER + accessToken))
                .andExpect(status().isOk()).andReturn();

        //then
        Map<String, Object> map = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 없습니다"));
        assertThat(user.getUsername()).isEqualTo(map.get("username"));
        assertThat(user.getNickname()).isEqualTo(map.get("nickname"));

    }

    @Test
    public void 내정보조회_실패_JWT없음() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new UserSignUpDto(username, password, nickName));
        signUp(signUpData);

        String accessToken = getAccessToken();

        //when,then
        mockMvc.perform(
                        get("/user")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(accessHeader, BEARER + accessToken+1))
                .andExpect(status().isForbidden());
    }

    /**
     * 회원정보조회 성공
     * 회원정보조회 실패 -> 회원이없음
     * 회원정보조회 실패 -> 권한이없음
     */
    @Test
    public void 회원정보조회_성공() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new UserSignUpDto(username, password, nickName));
        signUp(signUpData);

        String accessToken = getAccessToken();

        Long id = userRepository.findAll().get(0).getId();

        //when

        MvcResult result = mockMvc.perform(
                        get("/user/"+id)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(accessHeader, BEARER + accessToken))
                .andExpect(status().isOk()).andReturn();

        //then
        Map<String, Object> map = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new Exception("회원이 없습니다"));
        assertThat(user.getUsername()).isEqualTo(map.get("username"));
        assertThat(user.getNickname()).isEqualTo(map.get("nickname"));
    }

    @Test
    public void 회원정보조회_실패_없는회원조회() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new UserSignUpDto(username, password, nickName));
        signUp(signUpData);

        String accessToken = getAccessToken();

        //when
        MvcResult result = mockMvc.perform(
                        get("/user/2211")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(accessHeader, BEARER + accessToken))
                .andExpect(status().isBadRequest()).andReturn();

        //then
        Map<String, Integer> map = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        assertThat(map.get("errorCode")).isEqualTo(UserExceptionType.NOT_FOUND_USER.getErrorCode());//빈 문자열
    }


    @Test
    public void 회원정보조회_실패_JWT없음() throws Exception {
        //given
        String signUpData = objectMapper.writeValueAsString(new UserSignUpDto(username, password, nickName));
        signUp(signUpData);

        String accessToken = getAccessToken();

        //when,then
        mockMvc.perform(
                        get("/user/1")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(accessHeader, BEARER + accessToken+1))
                .andExpect(status().isForbidden());
    }
}
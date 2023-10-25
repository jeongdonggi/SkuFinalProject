package com.SecurityBoardEx.BoardEx.login.service;

import com.SecurityBoardEx.BoardEx.login.exception.UserException;
import com.SecurityBoardEx.BoardEx.login.exception.UserExceptionType;
import com.SecurityBoardEx.BoardEx.login.dto.UserInfoDto;
import com.SecurityBoardEx.BoardEx.login.dto.UserSignUpDto;
import com.SecurityBoardEx.BoardEx.login.dto.UserUpdateDto;
import com.SecurityBoardEx.BoardEx.login.entity.Role;
import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class UserServiceTest {


    @PersistenceContext
    EntityManager em;
    @Autowired
    UserRepository userRepository;

    @Autowired UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    String PASSWORD = "password";

    private void clear(){
        em.flush();
        em.clear();
    }

    private UserSignUpDto makeUserSignUpDto() {
        return new UserSignUpDto("username",PASSWORD,"name");
    }

    private UserSignUpDto setUser() throws Exception {
        UserSignUpDto UserSignUpDto = makeUserSignUpDto();
        userService.signUp(UserSignUpDto); // 로그인
        clear();
        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();

        emptyContext.setAuthentication(new UsernamePasswordAuthenticationToken(User.builder()
                .username(UserSignUpDto.username())
                .password(UserSignUpDto.password())
                .roles(Role.USER.name())
                .build(),
                null, null));

        SecurityContextHolder.setContext(emptyContext); // 회원가입 했으니 올려놓음
        return UserSignUpDto;
    }


    @AfterEach
    public void removeUser(){
        SecurityContextHolder.createEmptyContext().setAuthentication(null);
    }

    /**
     * 회원가입
     *    회원가입 시 아이디, 비밀번호, 이름, 별명, 나이를 입력하지 않으면 오류
     *    이미 존재하는 아이디가 있으면 오류
     *    회원가입 후 회원의 ROLE 은 USER
     *
     *
     */
    @Test
    public void 회원가입_성공() throws Exception {
        //given
        UserSignUpDto userSignUpDto = makeUserSignUpDto();

        //when
        userService.signUp(userSignUpDto);
        clear();

        //then  TODO : 여기 UserEXCEPTION으로 고치기
        UserEntity User = userRepository.findByUsername(userSignUpDto.username()).orElseThrow(() ->
                new UserException(UserExceptionType.NOT_FOUND_USER));
        assertThat(User.getId()).isNotNull();
        assertThat(User.getUsername()).isEqualTo(userSignUpDto.username());
        assertThat(User.getNickname()).isEqualTo(userSignUpDto.nickname());
        assertThat(User.getRole()).isSameAs(Role.USER);
    }

    @Test
    public void 회원가입_실패_원인_아이디중복() throws Exception {
        //given
        UserSignUpDto userSignUpDto = makeUserSignUpDto();
        userService.signUp(userSignUpDto);
        clear();

        //when, then TODO : UserException으로 고쳐야 함
        assertThat(assertThrows(UserException.class, () -> userService.signUp(userSignUpDto)).getExceptionType()).isEqualTo(UserExceptionType.ALREADY_EXIST_USERNAME);

    }

    @Test
    public void 회원가입_실패_입력하지않은_필드가있으면_오류() throws Exception {
        //given
        UserSignUpDto userSignUpDto1 = new UserSignUpDto(null,passwordEncoder.encode(PASSWORD),"nickname"); // 아이디 없음
        UserSignUpDto userSignUpDto2 = new UserSignUpDto("username",null,"nickNAme"); // 비밀번호 없음
        UserSignUpDto userSignUpDto3 = new UserSignUpDto("username",passwordEncoder.encode(PASSWORD),null); // 실명 없음


        //when, then

        assertThrows(Exception.class, () -> userService.signUp(userSignUpDto1));

        assertThrows(Exception.class, () -> userService.signUp(userSignUpDto2));

        assertThrows(Exception.class, () -> userService.signUp(userSignUpDto3));
    }
    /**
     * 회원정보수정
     * 회원가입을 하지 않은 사람이 정보수정시 오류 -> 시큐리티 필터가 알아서 막아줄거임
     * 아이디는 변경 불가능
     * 비밀번호 변경시에는, 현재 비밀번호를 입력받아서, 일치한 경우에만 바꿀 수 있음
     * 비밀번호 변경시에는 오직 비밀번호만 바꿀 수 있음
     *
     * 비밀번호가 아닌 이름,별명,나이 변경 시에는, 3개를 한꺼번에 바꿀 수도 있고, 한,두개만 선택해서 바꿀수도 있음
     * 아무것도 바뀌는게 없는데 변경요청을 보내면 오류
     *
     */
    @Test
    public void 회원수정_비밀번호수정_성공() throws Exception {
        //given
        UserSignUpDto UserSignUpDto = setUser();


        //when
        String toBePassword = "1234567890!@#!@#";
        userService.updatePassword(PASSWORD, toBePassword);
        clear();

        //then
        UserEntity findUser = userRepository.findByUsername(UserSignUpDto.username()).orElseThrow(() -> new Exception());
        assertThat(findUser.matchPassword(passwordEncoder, toBePassword)).isTrue();
    }



    @Test
    public void 회원수정_별명만수정() throws Exception {
        //given
        UserSignUpDto UserSignUpDto = setUser();

        //when
        String updateNickName = "변경할래용";
        userService.update(new UserUpdateDto(Optional.of(updateNickName)));
        clear();

        //then
        userRepository.findByUsername(UserSignUpDto.username()).ifPresent((user -> {
            assertThat(user.getNickname()).isEqualTo(updateNickName);
        }));
    }

    /**
     * 회원탈퇴
     * 비밀번호를 입력받아서 일치하면 탈퇴 가능
     */

    @Test
    public void 회원탈퇴() throws Exception {
        //given
        UserSignUpDto userSignUpDto = setUser();

        //when
        userService.withdraw(PASSWORD);

        //then
        assertThat(assertThrows(Exception.class, ()-> userRepository.findByUsername(userSignUpDto.username()).orElseThrow(() -> new Exception("회원이 없습니다"))).getMessage()).isEqualTo("회원이 없습니다");

    }

    @Test
    public void 회원탈퇴_실패_비밀번호가_일치하지않음() throws Exception {
        //given
        UserSignUpDto userSignUpDto = setUser();

        //when, then TODO : UserException으로 고쳐야 함
        assertThat(assertThrows(UserException.class ,() -> userService.withdraw(PASSWORD+"1")).getExceptionType()).isEqualTo(UserExceptionType.WRONG_PASSWORD);

    }




    @Test
    public void 회원정보조회() throws Exception {
        //given
        UserSignUpDto userSignUpDto = setUser();
        UserEntity user = userRepository.findByUsername(userSignUpDto.username()).orElseThrow(() -> new Exception());
        clear();

        //when
        UserInfoDto info = userService.getInfo(user.getId());

        //then
        assertThat(info.getUsername()).isEqualTo(userSignUpDto.username());
        assertThat(info.getNickname()).isEqualTo(userSignUpDto.nickname());
        System.out.println("이거 되면 안되는데");
        System.out.println("user.getPassword() = " + user.getPassword());

    }

    @Test
    public void 내정보조회() throws Exception {
        //given
        UserSignUpDto userSignUpDto = setUser();

        //when
        UserInfoDto myInfo = userService.getMyInfo();

        //then
        assertThat(myInfo.getUsername()).isEqualTo(userSignUpDto.username());
        assertThat(myInfo.getNickname()).isEqualTo(userSignUpDto.nickname());
    }

}
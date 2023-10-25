package com.SecurityBoardEx.BoardEx.login.service;

import com.SecurityBoardEx.BoardEx.config.SecurityUtil;
import com.SecurityBoardEx.BoardEx.login.dto.UserUpdateAuthDto;
import com.SecurityBoardEx.BoardEx.login.entity.Role;
import com.SecurityBoardEx.BoardEx.login.exception.UserException;
import com.SecurityBoardEx.BoardEx.login.exception.UserExceptionType;
import com.SecurityBoardEx.BoardEx.login.dto.UserInfoDto;
import com.SecurityBoardEx.BoardEx.login.dto.UserSignUpDto;
import com.SecurityBoardEx.BoardEx.login.dto.UserUpdateDto;
import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** 회원 가입 **/
    public void signUp(UserSignUpDto userDto) throws Exception{
        UserEntity user = userDto.toEntity(); // user 생성
        user.addUserAuthority(); // 유저 권한 부여
        user.passwordEncode(passwordEncoder);

        if(userRepository.findByUsername(userDto.username()).isPresent()){
            throw new UserException(UserExceptionType.ALREADY_EXIST_USERNAME);
        }

        userRepository.save(user); //userEntity 저장
    }

    /** 로그인 **/


    /** 실명 변경 **/
    public void update(UserUpdateDto userDto) throws Exception{
        // 이미 인증된 회원 아이디를 가져와 DB에서 찾는다. ( 보안 상승 )
        UserEntity user = userRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(()->
                new UserException(UserExceptionType.NOT_FOUND_USER));

        userDto.nickname().ifPresent(user::updateNickname);
    }

    /** 비밀번호 변경 **/
    public void updatePassword(String checkPassword, String toBePassword) throws Exception{
        UserEntity user = userRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(()->
                new UserException(UserExceptionType.NOT_FOUND_USER));

        if(!user.matchPassword(passwordEncoder, checkPassword)){
            throw  new UserException(UserExceptionType.WRONG_PASSWORD);
        }

        user.updatePassword(passwordEncoder, toBePassword);
    }

    /** 회원 탈퇴 **/
    public void withdraw(String checkPassword) throws Exception{
        UserEntity user = userRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(()->
                new UserException(UserExceptionType.NOT_FOUND_USER));

        if(!user.matchPassword(passwordEncoder, checkPassword)){
            throw new UserException(UserExceptionType.WRONG_PASSWORD);
        }

        userRepository.delete(user);
    }

    /** id를 이용한 다른 회원 정보 조회 **/
    public UserInfoDto getInfo(Long id) throws Exception{
        UserEntity findUser = userRepository.findById(id).orElseThrow(() ->
                new UserException(UserExceptionType.NOT_FOUND_USER));
        return new UserInfoDto(findUser);
    }

    /** 현재 나의 정보 조회 **/
    public UserInfoDto getMyInfo() throws Exception{
        UserEntity findUser = userRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(()->
                new UserException(UserExceptionType.NOT_FOUND_USER));
        return new UserInfoDto(findUser);
    }

    /** 전체 정보 조회 **/
    public List<UserInfoDto> findAll(){
        List<UserEntity> userEntityList = userRepository.findAll();
        List<UserInfoDto> userInfoDtoList = new ArrayList<>();
        for(UserEntity userEntity : userEntityList){
            userInfoDtoList.add(new UserInfoDto(userEntity));
        }

        return userInfoDtoList;
    }

    /** 권한 변경 **/
    public void updateAuth(UserUpdateAuthDto userDto){
        UserEntity findUser = userRepository.findById(userDto.userId()).orElseThrow(() ->
                new UserException(UserExceptionType.NOT_FOUND_USER));

        // userRole : 0 : GUEST, 1: USER, 2 : MANGER, 3 : ADMIN
        if(userDto.userRole().equals("USER")){
            findUser.authorizeUser(Role.USER);
        } else if (userDto.userRole().equals("MANAGER")) {
            findUser.authorizeUser(Role.MANAGER);
        } else if (userDto.userRole().equals("ADMIN")){
            findUser.authorizeUser(Role.ADMIN);
        } else { // 0 or 나머지 값
            findUser.authorizeUser(Role.GUEST);
        }

        userRepository.saveAndFlush(findUser);
    }

    /** 쿠키 만료 값 변경으로 삭제 **/
    public void invalidateCookie(String cookieName, HttpServletResponse response) {
        System.out.println("cookieName = " + cookieName);
        Cookie cookie = new Cookie(cookieName, null); // 쿠키의 값을 null로 설정
        cookie.setMaxAge(0); // 쿠키의 만료 시간을 0으로 설정하여 즉시 만료시킴
        cookie.setHttpOnly(true); // JavaScript에서 접근할 수 없도록 설정
        response.addCookie(cookie); // 응답에 쿠키 추가
    }
}
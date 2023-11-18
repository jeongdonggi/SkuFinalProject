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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "@$!%*?&#";
    private static final int MIN_LENGTH = 8;

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

    /** 실명 변경 **/
    public void update(UserUpdateDto userDto) throws Exception{
        // 이미 인증된 회원 아이디를 가져와 DB에서 찾는다. ( 보안 상승 )
        UserEntity user = userRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(()->
                new UserException(UserExceptionType.NOT_FOUND_USER));

        userDto.nickname().ifPresent(user::updateNickname);

        userRepository.save(user);
    }

    /** 비밀번호 변경 **/
    public void updatePassword(String checkPassword, String toBePassword) throws Exception{
        UserEntity user = userRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(()->
                new UserException(UserExceptionType.NOT_FOUND_USER));

        if(!user.matchPassword(passwordEncoder, checkPassword)){
            throw  new UserException(UserExceptionType.WRONG_PASSWORD);
        }

        user.updatePassword(passwordEncoder, toBePassword);

        userRepository.save(user);
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

    /** 회원 삭제 **/
    public void authWithdraw(Long userId){

        UserEntity user = userRepository.findById(userId).orElseThrow(() ->{
            throw  new UserException(UserExceptionType.NOT_FOUND_USER);
        });

        userRepository.delete(user);
    }

    /** id를 이용한 다른 회원 정보 조회 **/
    public UserInfoDto getInfo(Long id) throws Exception{
        UserEntity findUser = userRepository.findById(id).orElseThrow(() ->
                new UserException(UserExceptionType.NOT_FOUND_USER));
        return new UserInfoDto(findUser);
    }

    /** 이름을 이용한 Id 조회 **/
    public Long findByUsername(){
        UserEntity findUser = userRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(()->
                new UserException(UserExceptionType.NOT_FOUND_USER));
        return findUser.getId();
    }

    /** 비밀번호 찾기 **/
    public String updateToPassByUsername(String username){
        UserEntity findUser = userRepository.findByUsername(username).orElseThrow(() ->
                new UserException(UserExceptionType.NOT_FOUND_USER));

        String updatePass = generatePassword();

        findUser.updatePassword(passwordEncoder, updatePass);

        userRepository.save(findUser);

        return updatePass;
    }

    /** 비밀번호 생성 **/
    public String generatePassword() {
        SecureRandom random = new SecureRandom();
        List<Character> chars = new ArrayList<>(); // 비밀번호

        // 각 카테고리에서 최소 하나의 문자 추가
        chars.add(LOWER.charAt(random.nextInt(LOWER.length())));
        chars.add(UPPER.charAt(random.nextInt(UPPER.length())));
        chars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        chars.add(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));

        // 나머지 길이 채우기
        for (int i = 4; i < MIN_LENGTH; i++) {
            String combinedChars = LOWER + UPPER + DIGITS + SPECIAL_CHARACTERS;
            chars.add(combinedChars.charAt(random.nextInt(combinedChars.length())));
        }

        // 더 강력한 보안을 위해 문자 위치 섞기
        Collections.shuffle(chars);

        // 최종 문자열 생성
        StringBuilder password = new StringBuilder();
        for (char c : chars) {
            password.append(c);
        }

        return password.toString();
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

    /** 나의 권한 변경 **/
    public void updateMyAuth(){
        UserEntity findUser = userRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(()->
                new UserException(UserExceptionType.NOT_FOUND_USER));

        findUser.authorizeUser(Role.ADMIN);

        userRepository.save(findUser);
    }

    /** 권한 변경 **/
    public void updateAuth(UserUpdateAuthDto userDto){
        UserEntity findUser = userRepository.findById(userDto.userId()).orElseThrow(() ->
                new UserException(UserExceptionType.NOT_FOUND_USER));

        log.info("userRole : {}", userDto.userRole());

        if(userDto.userRole().equals("SEMIUSER")){
            findUser.authorizeUser(Role.SEMIUSER);
        } else if(userDto.userRole().equals("USER")){
            findUser.authorizeUser(Role.USER);
        }else if (userDto.userRole().equals("MANAGER")) {
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
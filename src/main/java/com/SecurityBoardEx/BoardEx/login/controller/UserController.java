package com.SecurityBoardEx.BoardEx.login.controller;

import com.SecurityBoardEx.BoardEx.login.dto.*;
import com.SecurityBoardEx.BoardEx.login.entity.Role;
import com.SecurityBoardEx.BoardEx.login.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    /** 회원 가입 **/
    @PostMapping("/signUp")
    @ResponseStatus(HttpStatus.OK)
    public void signUp(@Valid @RequestBody UserSignUpDto userDto) throws Exception{
        System.out.println("UserController.signUp");
        System.out.println("userDto = " + userDto);
        userService.signUp(userDto);
    }

    /** 회원 실명 정보 수정 **/
    @PutMapping("/user/name")
    @ResponseStatus(HttpStatus.OK)
    public void updateBasicInfo(@Valid @RequestBody UserUpdateDto userDto) throws Exception{
        userService.update(userDto);
    }

    /** 비밀번호 수정 **/
    @PutMapping("/user/password")
    @ResponseStatus(HttpStatus.OK)
    public void updatePassword(@Valid @RequestBody UserUpdatePasswordDto userDTo) throws Exception{
        userService.updatePassword(userDTo.checkPassword(), userDTo.toBePassword());
    }

    /** 회원 탈퇴 **/
    @DeleteMapping("/withdraw")
    @ResponseStatus(HttpStatus.OK)
    public void withDraw(@Valid @RequestBody UserWithdrawDto userDto, HttpServletResponse response) throws Exception{
        userService.withdraw(userDto.checkPassword());
        userService.invalidateCookie("AccessToken", response);
        userService.invalidateCookie("RefreshToken", response);
    }

    /** 회원 삭제 **/
    @DeleteMapping("/AuthWithdraw")
    @ResponseStatus(HttpStatus.OK)
    public void AuthWithdraw(@RequestBody UserAuthIdDto userIdDto){
        userService.authWithdraw(userIdDto.getUserId());
    }

    /** 로그 아웃 **/
    @PostMapping("/signout")
    public ResponseEntity logout(HttpServletResponse response) {
        log.info("UserController.logout");
        userService.invalidateCookie("AccessToken", response);
        userService.invalidateCookie("RefreshToken", response);

        return ResponseEntity.ok().build();
    }

    /** 권한 변경 **/
    @PostMapping("/updateauthorization")
    public ResponseEntity updateAuth(@RequestBody UserUpdateAuthDto userDto){
        log.info("updateauth 접근: {}", userDto.toString());
        userService.updateAuth(userDto);
        return ResponseEntity.ok().build();
    }
}
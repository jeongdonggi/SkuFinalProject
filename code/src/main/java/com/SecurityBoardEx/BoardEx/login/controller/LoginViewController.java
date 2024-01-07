package com.SecurityBoardEx.BoardEx.login.controller;

import com.SecurityBoardEx.BoardEx.login.dto.UserInfoDto;
import com.SecurityBoardEx.BoardEx.login.dto.UserSignUpDto;
import com.SecurityBoardEx.BoardEx.login.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginViewController {

    @Value("${admin.approve-code}")
    private String approveCode;
    private final UserService userService;

    // 회원 가입
    @GetMapping("/signup")
    public String viewSignUpPage(){
        System.out.println("signup view 들어옴");
        return "login/signup";
    }

    // 로그인
    @GetMapping("/login")
    public String viewLoginPage(){
        System.out.println("login view 들어옴");
        return "login/login";
    }

    // 비밀번호 변경
    @GetMapping("/user/updatepw")
    public String updatePasswordPage(){
        System.out.println("updatePassword view 들어옴");
        return "login/updatepassword";
    }

    // 비밀번호 초기화
    @GetMapping("/password")
    public String updatePass(){
        return "login/password";
    }

    // 이름 변경
    @GetMapping("/user/updatename")
    public String updateNamePage(){
        System.out.println("updateName view 들어옴");
        return "login/updateusername";
    }

    // 내 정보 조회
    @GetMapping("/user/myInfo")
    public String getMyInfo(Model model) throws Exception{
        UserInfoDto info = userService.getMyInfo();
        model.addAttribute("user", info);
        return "login/mydetail";
    }

    // 회원 정보 조회
    @GetMapping("/user/{id}")
    public String getInfo(@Valid @PathVariable("id") Long id, Model model) throws Exception{
        UserInfoDto info = userService.getInfo(id);
        model.addAttribute("user", info);
        return "login/detail";
    }

    // 회원 목록
    @GetMapping("/user/list")
    public String findAll(Model model){
        List<UserInfoDto> userList = userService.findAll();
        model.addAttribute("userList", userList);
        return "login/list";
    }

    // 회원 탈퇴
    @GetMapping("/user/withdraw")
    public String withDrawPage(){
        return "login/withdraw";
    }

    // 권한 변경
    @GetMapping("/user/updateAuth")
    public String updateAuthPage(Model model) throws Exception {
        UserInfoDto myInfo = userService.getMyInfo();
        String role = myInfo.getRole();
        List<UserInfoDto> userList = userService.findAll();
        model.addAttribute("userList", userList);
        if(role == "ADMIN"){
            return "login/listauth";
        }
        return "login/listauthmanager";
    }

    /** 자신의 권한 변경 **/
    @PostMapping("/adminchange")
    public String updateMyAuth(@RequestParam("adminKey") String adminKey) throws Exception {
        log.info("admincode : {}", approveCode);
        log.info("getadmincode : {}", adminKey);
        if(approveCode.equals(adminKey)){
            userService.updateMyAuth();
        }
        return "redirect:/user/myInfo";
    }
}

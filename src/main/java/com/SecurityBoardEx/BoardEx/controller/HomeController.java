package com.SecurityBoardEx.BoardEx.controller;

import com.SecurityBoardEx.BoardEx.login.dto.UserInfoDto;
import com.SecurityBoardEx.BoardEx.login.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;

    @GetMapping("/")
    public String index(){
        System.out.println("index.html 진입");

        return "index";
    }

    @GetMapping("/main")
    public String main(Model model){
        List<UserInfoDto> userList = userService.findAll();
        model.addAttribute("userList", userList);
        return "mainpage";
    }
}

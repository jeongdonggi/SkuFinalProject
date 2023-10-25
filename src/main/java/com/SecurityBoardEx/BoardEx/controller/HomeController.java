package com.SecurityBoardEx.BoardEx.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(){
        System.out.println("index.html 진입");
        return "index";
    }
}

package com.SecurityBoardEx.BoardEx.mail.controller;

import com.SecurityBoardEx.BoardEx.login.dto.UserInfoDto;
import com.SecurityBoardEx.BoardEx.login.service.UserService;
import com.SecurityBoardEx.BoardEx.mail.dto.EmailDto;
import com.SecurityBoardEx.BoardEx.mail.dto.EmailRequestDto;
import com.SecurityBoardEx.BoardEx.mail.service.MailService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RestController
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;
    private final UserService userService;
    private final TemplateEngine templateEngine;

    @PostMapping("/password")
    public ResponseEntity sendMail(@RequestBody EmailRequestDto emailRequest) throws Exception {
        Context context = new Context();

        String username = emailRequest.getUsername();

        String userPass = userService.updateToPassByUsername(username);

        context.setVariable("username",username);
        context.setVariable("userPass", userPass);
        String message = templateEngine.process("mail/mail-password", context);

        EmailDto emailMessage = EmailDto.builder()
                .to(username)
                .subject("find password")
                .message(message)
                .build();
        mailService.sendMail(emailMessage);
        return new ResponseEntity(HttpStatus.OK);
    }
}

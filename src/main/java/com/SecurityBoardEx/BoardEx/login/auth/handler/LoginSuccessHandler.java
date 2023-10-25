package com.SecurityBoardEx.BoardEx.login.auth.handler;

import com.SecurityBoardEx.BoardEx.login.auth.service.JwtService;
import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.UnsupportedEncodingException;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${jwt.access.expiration}")
    private String accessTokenExpiration;

    private static final String BEARER = "Bearer ";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws UnsupportedEncodingException {
        String username = extractUsername(authentication); // 인증정보에서 email 추출
        String accessToken = jwtService.createAccessToken(username); // 엑세스 토큰 생성
        String refreshToken = jwtService.createRefreshToken(); // 리프레시 토큰 생성

        /** 헤더에 값 넣어 보내기 **/
        // jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken); // 응답 시 보내줌

        /** 쿠키 헤더에 값 넣어 보내기 **/
        jwtService.sendAccessAndRefreshTokenInCookie(response,BEARER + accessToken,BEARER + refreshToken);

        userRepository.findByUsername(username)
                .ifPresent(user -> {
                    user.updateRefreshToken(BEARER + refreshToken);
                    userRepository.saveAndFlush(user); // 리프레시 토큰 업데이트 후 바로 저장
                });
        log.info("로그인에 성공하였습니다. 이메일 : {}", username);
        log.info("로그인에 성공하였습니다. AccessToken : {}", accessToken);
        log.info("로그인에 성공하였습니다. refreshToken : {}", refreshToken);
        log.info("발급된 AccessToken 만료 기간 : {}", System.currentTimeMillis() + accessTokenExpiration);
    }

    private String extractUsername(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
package com.SecurityBoardEx.BoardEx.login.auth.handler;

import com.SecurityBoardEx.BoardEx.login.auth.service.JwtService;
import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import com.SecurityBoardEx.BoardEx.login.exception.UserException;
import com.SecurityBoardEx.BoardEx.login.exception.UserExceptionType;
import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import com.SecurityBoardEx.BoardEx.login.service.UserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpiration;

    private static final String BEARER = "Bearer ";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws UnsupportedEncodingException {

        String username = extractUsername(authentication); // 인증정보에서 email 추출
        UserEntity byUsername = userRepository.findByUsername(username).orElseThrow(() ->
                new UserException(UserExceptionType.NOT_FOUND_USER));
        String accessToken = jwtService.createAccessToken(username); // 엑세스 토큰 생성
        String refreshToken = jwtService.createRefreshToken(byUsername.getId()); // 리프레시 토큰 생성

        /** 쿠키에 값 넣어 보내기 **/
        jwtService.sendAccessAndRefreshTokenInCookie(response,BEARER + accessToken,BEARER + refreshToken);

        userRepository.findByUsername(username)
                .ifPresent(user -> {
                    user.updateRefreshToken(BEARER + refreshToken);
                    userRepository.saveAndFlush(user); // 리프레시 토큰 업데이트 후 바로 저장
                });
        log.info("로그인에 성공하였습니다. 이메일 : {}", username);
        log.info("로그인에 성공하였습니다. AccessToken : {}", accessToken);
        log.info("로그인에 성공하였습니다. refreshToken : {}", refreshToken);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date expirationDate = new Date(System.currentTimeMillis() + accessTokenExpiration);
        log.info("발급된 AccessToken 만료 기간 : {}", sdf.format(expirationDate));


        // JWT 라이브러리를 사용하여 토큰의 만료 시간을 가져옵니다.
        DecodedJWT decodedJWT = JWT.decode(accessToken);
        Date actualExpirationDate = decodedJWT.getExpiresAt();

        // 가져온 Date 객체를 원하는 형식으로 포맷하여 출력합니다.
        log.info("실제 AccessToken 만료 기간 : {}", sdf.format(actualExpirationDate));

    }

    private String extractUsername(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
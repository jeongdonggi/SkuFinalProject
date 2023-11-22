package com.SecurityBoardEx.BoardEx.login.auth.service;

import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Setter(value = AccessLevel.PRIVATE)
public class JwtService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String USERNAME_CLAIM = "username";
    private static final String USERID_CLAIM = "userId";
    private static final String BEARER = "Bearer ";

    /** 엑세스 토큰 생성 **/
    public String createAccessToken(String username){
        String token =  JWT.create()
                .withSubject(ACCESS_TOKEN_SUBJECT)
                .withExpiresAt(new Date(System.currentTimeMillis()+ accessTokenExpirationPeriod))
                .withClaim(USERNAME_CLAIM, username) // 클레임 이름, 클레임 값
                .sign(Algorithm.HMAC512(secretKey)); //HMAC512 알고리즘 사용
        return token;
    }

    /** 리프레시 토큰 생성 **/
    public String createRefreshToken(Long userId){
        String token =  JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpirationPeriod))
                .withClaim(USERID_CLAIM ,userId)
                .sign(Algorithm.HMAC512(secretKey));
        return token;
    }

    // 이제 헤더에 넣는거 안씁니다. 쿠키로 바꿨습니다.
    /** 헤더에 accessToken 넣기 **/
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, accessToken);
    }
    /** 헤더에 refreshToken 넣기 **/
    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, refreshToken);
    }

    /** AccessToken 헤더에 실어서 보내기 **/
    public void sendAccessToken(HttpServletResponse response, String accessToken){
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);
        log.info("재발급된 Access Token : {}", accessToken);
    }

    /** AccessToken + RefreshToken 헤더에 실어서 보내기 **/
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken){
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);
        log.info("Access Token, Refresh Token 헤더 설정 완료");
    }

    /** 헤더에서 AccessToken 추출 **/
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(BEARER))
                .map(accessToken -> accessToken.replace(BEARER, ""));
    }

    /** 헤더에서 RefreshToken 추출 **/
    public Optional<String> extractRefreshToken(HttpServletRequest request){
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    /** AccessToken에서 Username 추출
     * JWT.require()로 검증기 생성 -> verify로 검증 -> 유효하면 이메일 추출 , 아니면 빈 Optional 반환
     * **/
    public Optional<String> extractUsername(String accessToken){
        try {
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(accessToken)// 여기서 예외 발생
                    .getClaim(USERNAME_CLAIM)
                    .asString());
        } catch (Exception e){
            log.info("엑세스 토큰이 유효하지 않습니다. {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** 토큰 유효성 검사 **/
    public Boolean isTokenValid(String token){
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            log.info("유효한 토큰 입니다.");
            return true;
        } catch (Exception e){
            log.info("유효하지 않은 토큰 입니다. {}", e.getMessage());
            return false;
        }
    }

    /** 쿠키로 뽑아봅시다 이제 위에서 쓰는 헤더 부분은 사용 안합니다. **/

    /** 쿠키를 이용한 엑세스토큰 변경 **/
    public void setAccessTokenInCookie(HttpServletResponse response, String AccessTokenName, String tokenValue)
            throws UnsupportedEncodingException {
        Cookie cookie = new Cookie(AccessTokenName,
                URLEncoder.encode(tokenValue, StandardCharsets.UTF_8.toString()));
        cookie.setPath("/");
        cookie.setHttpOnly(true); // js접근 막음
        cookie.setMaxAge(Math.toIntExact(accessTokenExpirationPeriod / 1000));
        response.addCookie(cookie);
    }

    /** 쿠키를 이용한 리프레시토큰 변경 **/
    public void setRefreshTokenInCookie(HttpServletResponse response, String RefreshTokenName, String tokenValue)
            throws UnsupportedEncodingException{
        Cookie cookie = new Cookie(RefreshTokenName,
                URLEncoder.encode(tokenValue, StandardCharsets.UTF_8.toString()));
        cookie.setPath("/");
        cookie.setHttpOnly(true); // js접근 막음
        cookie.setMaxAge(Math.toIntExact(refreshTokenExpirationPeriod / 1000));
        response.addCookie(cookie);
    }

    /** AccessToken + RefreshToken 쿠키에 실어서 보내기 **/
    public void sendAccessAndRefreshTokenInCookie(HttpServletResponse response, String accessToken, String refreshToken) throws UnsupportedEncodingException {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenInCookie(response, "AccessToken", accessToken);
        setRefreshTokenInCookie(response, "RefreshToken", refreshToken);
        log.info("Access Token, Refresh Token 쿠키 설정 완료");
    }

    /** jwt 추출 메서드 **/
    public Optional<String> extractTokenFromCookie(HttpServletRequest request, String tokenName) throws UnsupportedEncodingException {
        if(request.getCookies() != null){
            for(Cookie cookie : request.getCookies()){
                if(cookie.getName().equals(tokenName)){
                    return Optional.of(URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8.toString())
                            .replace(BEARER, ""));
                }
            }
        }
        return Optional.empty();
    }

    /** 쿠키에서 값을 가져오는 메서드 **/
    public Optional<String> extractAccessTokenFormCookie(HttpServletRequest request) throws UnsupportedEncodingException {
        return extractTokenFromCookie(request, "AccessToken");
    }

    public Optional<String> extractRefreshTokenFromCookie(HttpServletRequest request) throws UnsupportedEncodingException {
        return extractTokenFromCookie(request, "RefreshToken");
    }
}
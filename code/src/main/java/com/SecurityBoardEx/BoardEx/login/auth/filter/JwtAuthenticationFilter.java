package com.SecurityBoardEx.BoardEx.login.auth.filter;

import com.SecurityBoardEx.BoardEx.login.auth.service.JwtService;
import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Set<String> NO_CHECK_URLS = Set.of(
            "/login",
            "/signup",
            "/password",
            "/favicon.ico",
            "/error",
            "/"
    );

    private static final String BEARER = "Bearer ";
    private static final String UNUSED_PASSWORD = "UNUSED_PASSWORD";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    // 리프레시 토큰이 오는 경우 -> 유효하면 accessToken 재발급 후, 필터 진행 x
    //리프레시 토큰은 없고 AccessToken만 있는 경우 -> 유저정보 저장 후 필터 계속 진행
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();

        // 정적 리소스에 대한 요청은 인증 절차를 건너뛰고 필터 체인을 계속 진행합니다.
        if (isStaticResource(path)) {
            filterChain.doFilter(request, response);
            return;
        }


        // 인증이 필요 없는 URL인 경우, 필터 체인을 계속 진행합니다.
        if (NO_CHECK_URLS.contains(path) ) {
            log.info("인증이 필요 없습니다.");
            // JWT를 검사하여 인증을 시도합니다.
            Optional<String> possibleToken = jwtService.extractAccessTokenFormCookie(request);
            if (possibleToken.isPresent() && jwtService.isTokenValid(possibleToken.get())) {
                // Token이 유효하면, 사용자가 이미 인증된 것입니다. 메인 페이지로 리다이렉트합니다.
                Optional<String> username = jwtService.extractUsername(possibleToken.get());
                if (username.isPresent()) {
                    // 인증 성공 후 메인 페이지로 리다이렉트할 경우의 처리를 여기에 작성합니다.
                    response.sendRedirect("/main");
                    return;
                }
            }
            // 인증이 필요 없는 페이지로의 정상 요청 처리를 계속합니다.
            filterChain.doFilter(request, response);
            return;
        }

        /** 사용자 요청에서 RefreshToken 추출 -> RefreshToken이 있다 = AccessToken이 만료된 경우 => not null **/
        /** 변경점 : extractRefreshToken -> extractRefreshTokenFromCookie **/
        String refreshToken = jwtService.extractRefreshTokenFromCookie(request) // 리프레시 토큰 추출 - 변경
                .filter(jwtService::isTokenValid) // 리프레시 토큰이 존재할 경우
                .orElse(null); // 있으면 값 유지, 없으면 null

        String accessToken = jwtService.extractAccessTokenFormCookie(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);

        if(accessToken != null){
            checkAccessTokenAndAuthentication(request, response, filterChain);
        }else{
            /** RefreshToken이 있다면 = not null -> DB에서 Token이 일치하는지 판단 후 AccessToken 재발급 **/
            if(refreshToken != null){
                log.info("refresh토큰이 있기 떄문에 checkRefreshTokenAndReIssueAccessToken으로 들어옴");
                checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
                filterChain.doFilter(request, response);
                return;
            }
            // 다 없을 때 엑세스 거부가 뜨도록 엑세스 토큰 검사를 한 번 더 해줍니다.
            checkAccessTokenAndAuthentication(request, response, filterChain);
        }
    }

    private boolean isStaticResource(String path) {
        log.info("정적 리소스 경로를 확인하는 로직");
        // 예: "/css/", "/js/", "/images/", "/static/" 등
        return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") || path.startsWith("/static/");
    }

    /** 리프레시 토큰으로 유저 정보 찾기 + 엑세스 토큰/ 리프레시 토큰 재발급 메소드
     * 리프레시 토큰으로 DB에서 유저를 찾고, 해당 유저가 있다면 엑세스 토큰 생성
     * 리프레시 토큰 재발급 받고 DB에 업데이트 후 엑세스토큰과 리프레시 토큰 응답 헤더에 보내기 **/
    public void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {
        log.info("리프레시 토큰이 있습니다.");


        userRepository.findByRefreshToken(BEARER + refreshToken)
                .ifPresent(user -> {
                    String reIssuedRefreshToken = reIssueRefreshToken(user);
                    /** sendAccessAndRefreshToken -> sendAccessAndRefreshTokenInCookie **/
                    try {
                        String newAccessToken = BEARER + jwtService.createAccessToken(user.getUsername());
                        jwtService.sendAccessAndRefreshTokenInCookie(response, newAccessToken
                                , BEARER + reIssuedRefreshToken);
                        saveAuthentication(user); // 추가
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // 회전하는 리프레시 토큰 : 리프레시 토큰을 사용할 때마다 새로운 리프레시 토큰을 발급
    private String reIssueRefreshToken(UserEntity user) {
        log.info("reIssueRefreshToken 들어옴");
        String reIssuedRefreshToken = jwtService.createRefreshToken(user.getId());
        user.updateRefreshToken(BEARER + reIssuedRefreshToken);
        userRepository.saveAndFlush(user);
        return reIssuedRefreshToken;
    }

    /** 엑세스 토큰 체크 + 인증처리 메서드 **/
    private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                   FilterChain filterChain) throws ServletException, IOException {
        log.info("checkAccessTokenAndAuthentication() 호출");
        /** extractAccessToken -> extractAccessTokenFormCookie **/
        jwtService.extractAccessTokenFormCookie(request) // 엑세스 토큰 추출
                .filter(jwtService::isTokenValid) // 토큰이 유효하다면
                .ifPresent(accessToken -> jwtService.extractUsername(accessToken) // 이메일을 추출
                        .ifPresent(username -> userRepository.findByUsername(username) // 이메일에 맞는 객체 추출
                                .ifPresent(user -> saveAuthentication(user)))); // 인증 필터로 진행

        filterChain.doFilter(request, response);
    }

    /** [인증 허가 메소드] -> 이미 인증이 되어 있으므로 패스워드 필요 없음 **/
    private void saveAuthentication(UserEntity myUser) {
        
        log.info("saveAuthentication 진입");

        UserDetails userDetails = User.builder() //빌더의 유저 : UserDetails의 User 객체
                .username(myUser.getUsername())
                .password(UNUSED_PASSWORD) // 임시값을 설정해주어야 함
                .roles(myUser.getRole().name())
                .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken( // Authentication 객체 생성 ( 인증 객체 )
                        userDetails, // 유저 정보
                        null, // 인증 시에는 보통 null로 비밀번호를 제거
                        authoritiesMapper.mapAuthorities(userDetails.getAuthorities()));
        // 권한을 추출해서 GrantedAuthoritiesMapper 생성

        // 현재 스레드에서 실행되는 요청이 인증되었음을 시큐리티에 알림
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

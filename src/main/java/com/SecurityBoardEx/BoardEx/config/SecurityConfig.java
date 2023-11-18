package com.SecurityBoardEx.BoardEx.config;

import com.SecurityBoardEx.BoardEx.login.auth.filter.CustomUsernamePasswordAuthenticationFilter;
import com.SecurityBoardEx.BoardEx.login.auth.filter.JwtAuthenticationFilter;
import com.SecurityBoardEx.BoardEx.login.auth.handler.LoginFailureHandler;
import com.SecurityBoardEx.BoardEx.login.auth.handler.LoginSuccessHandler;
import com.SecurityBoardEx.BoardEx.login.auth.service.JwtService;
import com.SecurityBoardEx.BoardEx.login.auth.service.LoginService;
import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginService loginService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .exceptionHandling(exp ->
                        exp.accessDeniedPage("/error"))
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> // URL별 권한 관리 옵션
                        auth
                                .requestMatchers("/", "/signup", "/loginForm", "/password" ,"/favicon.ico", "/error").permitAll()
                                .requestMatchers("/login", "/signUp").permitAll()
                                .requestMatchers( "/files/**").hasAnyRole("USER", "ADMIN", "MANAGER")
                                .requestMatchers("/board/**").hasAnyRole("SEMIUSER","USER", "ADMIN", "MANAGER")
                                .requestMatchers("/updateauthorization").hasAnyRole("ADMIN", "MANAGER")
                                .requestMatchers("/user/updateAuth").hasAnyRole("ADMIN", "MANAGER")
                                .anyRequest().authenticated() // 인증된 사용자만 접근 가능
                )
                .addFilterAfter(customUsernamePasswordAuthenticationFilter(), LogoutFilter.class)
                .addFilterBefore(jwtAuthenticationProcessingFilter(), CustomUsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/images/**");
    }


    //BCryptPasswordEncoder를 사용하는 DelegatingPasswordEncoder를 사용하는 것임
    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // 사용자 인증을 위한 코드
    @Bean
    public AuthenticationManager authenticationManager(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(loginService);
        return new ProviderManager(provider);
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler(){
        return new LoginSuccessHandler(jwtService, userRepository);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler(){
        return new LoginFailureHandler();
    }

    // 로그인 성공시 핸들러와 실패시 핸들러 설정
    @Bean
    public CustomUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter(){
        CustomUsernamePasswordAuthenticationFilter customJsonUsernamePasswordAuthenticationFilter
                = new CustomUsernamePasswordAuthenticationFilter(objectMapper);
        customJsonUsernamePasswordAuthenticationFilter.setAuthenticationManager(authenticationManager());
        customJsonUsernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
        customJsonUsernamePasswordAuthenticationFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return customJsonUsernamePasswordAuthenticationFilter;
    }

    // jwt 인증 필터
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationProcessingFilter(){
        JwtAuthenticationFilter jwtAuthenticationProcessingFilter
                = new JwtAuthenticationFilter(jwtService, userRepository);
        return jwtAuthenticationProcessingFilter;
    }
}

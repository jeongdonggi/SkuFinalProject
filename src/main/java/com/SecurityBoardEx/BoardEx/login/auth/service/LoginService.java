package com.SecurityBoardEx.BoardEx.login.auth.service;

import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** 커스텀 로그인 구현 **/
@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("해당 이메일이 존재하지 않습니다."));

        // 로그인 시 호출됨으로 패스워드가 필요하다.
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name()) // 기본적으로 "ROLE_{}"를 구별해주는 코드가 있음
                .build();
    }
}

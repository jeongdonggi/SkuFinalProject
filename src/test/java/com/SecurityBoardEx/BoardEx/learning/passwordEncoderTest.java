package com.SecurityBoardEx.BoardEx.learning;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class passwordEncoderTest {

    @Autowired private PasswordEncoder passwordEncoder;

    // BcyptPasswordEncoder로 인코딩된 패스워드는 {bcrypy}를 시작부분에 포함한다.
    @Test
    public void 패스워드암호화() throws Exception{
        // given
        String password = "안녕hello";
        //when
        String encodePassword = passwordEncoder.encode(password);
        //then
        assertThat(encodePassword).startsWith("{");
        assertThat(encodePassword).contains("{bcrypt}");
        assertThat(encodePassword).isNotEqualTo(password);
    }

    @Test
    public void 패스워드_랜덤_암호화() throws Exception {
        //given
        String password = "신동훈ShinDongHun";
        //when
        String encodePassword = passwordEncoder.encode(password);
        String encodePassword2 = passwordEncoder.encode(password);

        //then
        assertThat(encodePassword).isNotEqualTo(encodePassword2);
    }

    @Test
    public void 암호화된_비밀번호_매치() throws Exception {
        //given
        String password = "신동훈ShinDongHun";

        //when
        String encodePassword = passwordEncoder.encode(password);

        //then
        assertThat(passwordEncoder.matches(password, encodePassword)).isTrue();

    }
}

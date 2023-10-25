package com.SecurityBoardEx.BoardEx.repository;

import com.SecurityBoardEx.BoardEx.login.entity.Role;
import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @PersistenceContext EntityManager em; // 테스트 시 이걸 사용해야 함


    private void clear(){
        em.flush();
        em.clear();
    }

    @AfterEach
    private void after(){
        em.clear();
    }

    @Test
    public void User_test_save() throws Exception{
        //given
        UserEntity user = UserEntity.builder().username("test@navet.com").password("1234").nickname("test")
                .refreshToken("asdf").role(Role.USER).build();
        //when
        UserEntity saveuser = userRepository.save(user);

        //then
        UserEntity findUser = userRepository.findById(saveuser.getId()).orElseThrow(()->
            new RuntimeException("저장 회원 없음")
        );

        assertThat(findUser).isSameAs(saveuser);
        assertThat(findUser).isSameAs(user);
    }

    @Test
    public void User_test_noneEmail() throws Exception{
        //given
        UserEntity user = UserEntity.builder().password("1234").nickname("test")
                .refreshToken("asdf").role(Role.USER).build();

        // then
        assertThrows(Exception.class, () -> userRepository.save(user));
        System.out.println("user = " + user);
    }

    @Test
    public void User_test_sameUsername() throws Exception{
        // given
        UserEntity user1 = UserEntity.builder().username("test1@navet.com").password("12341").nickname("test1")
                .refreshToken("asdf1").role(Role.USER).build();
        UserEntity user2 = UserEntity.builder().username("test1@navet.com").password("12342").nickname("test2")
                .refreshToken("asdf2").role(Role.USER).build();

        //when
        userRepository.save(user1);
        clear();

        //then
        assertThrows(Exception.class, () -> userRepository.save(user2));
    }
}
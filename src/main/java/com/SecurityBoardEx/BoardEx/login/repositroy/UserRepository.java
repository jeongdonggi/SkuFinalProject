package com.SecurityBoardEx.BoardEx.login.repositroy;

import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByRefreshToken(String refreshToken);

    boolean existsByUsername(String username);
}

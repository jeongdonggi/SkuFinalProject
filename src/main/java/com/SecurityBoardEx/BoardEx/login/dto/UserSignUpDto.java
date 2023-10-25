package com.SecurityBoardEx.BoardEx.login.dto;

import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Valid를 사욯하기 때문에 변경
public record UserSignUpDto(@NotBlank @Size(min = 7, max = 25)
                            @Pattern(regexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")
                            String username,
                            @NotBlank
                            // 소문자, 대문자, 숫자, 특수 문자 최소 1개 포함, 최소 8자리 이상
                            @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$")
                            String password,
                            @NotBlank @Pattern(regexp = "^[A-Za-z가-힣]+$") // 한글 또는 영문
                            String nickname) {

    public UserEntity toEntity() {
        return UserEntity.builder().username(username).password(password).nickname(nickname).build();
    }
}
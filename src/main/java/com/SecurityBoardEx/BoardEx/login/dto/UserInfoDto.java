package com.SecurityBoardEx.BoardEx.login.dto;

import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import lombok.*;

@Getter @Setter
@ToString
@NoArgsConstructor
public class UserInfoDto {

    private String username;
    private String nickname;
    private String role;
    private Long id;

    @Builder
    public UserInfoDto(UserEntity user){
        if(user == null){
            throw new NullPointerException("NullPointerException 발생");
        }
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.role = String.valueOf(user.getRole());
        this.id = user.getId();
    }
}

package com.SecurityBoardEx.BoardEx.login.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    GUEST("ROLE_GUEST") ,USER("ROLE_USER"), MANAGER("ROLE_MANAGER"), ADMIN("ROLE_ADMIN");

    private final String key;
}


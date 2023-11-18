package com.SecurityBoardEx.BoardEx.login.dto;

import jakarta.validation.constraints.NotBlank;

public record UserWithdrawDto(@NotBlank
                              String checkPassword) {
}

package com.SecurityBoardEx.BoardEx.login.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserUpdatePasswordDto(@NotBlank String checkPassword,
                                    @NotBlank
                                    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,30}$")
                                    String toBePassword) {
}

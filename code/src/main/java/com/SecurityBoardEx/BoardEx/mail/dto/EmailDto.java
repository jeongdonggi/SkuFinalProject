package com.SecurityBoardEx.BoardEx.mail.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class EmailDto {
    private String to; // 수신자
    private String subject; // 제목
    private String message; // 본문
}

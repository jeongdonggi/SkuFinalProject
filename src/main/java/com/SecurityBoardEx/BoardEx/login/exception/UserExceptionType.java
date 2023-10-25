package com.SecurityBoardEx.BoardEx.login.exception;

import com.SecurityBoardEx.BoardEx.exception.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum UserExceptionType implements BaseExceptionType {

    /** 회원 가입, 로그인 시 **/
    ALREADY_EXIST_USERNAME(600, HttpStatus.BAD_REQUEST, "이미 존재하는 아이디 입니다."),
    WRONG_PASSWORD(601, HttpStatus.BAD_REQUEST, "비밀번호가 잘못 되었습니다."),
    NOT_FOUND_USER(602, HttpStatus.BAD_REQUEST, "회원 정보가 없습니다.");

    private int errorCode;
    private HttpStatus httpStatus;
    private String errorMessage;

    UserExceptionType(int errorCode, HttpStatus httpStatus, String errorMessage) {
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    @Override
    public int getErrorCode() {
        return this.errorCode;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }
}

package com.SecurityBoardEx.BoardEx.chat.exception;

import com.SecurityBoardEx.BoardEx.exception.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ChatExceptionType implements BaseExceptionType {

    // 이 부분 변경
    NOT_FOUND_ROOM(900,HttpStatus.NOT_FOUND, "찾으시는 채팅방이 없습니다");

    private int errorCode;
    private HttpStatus httpStatus;
    private String errorMessage;

    ChatExceptionType(int errorCode, HttpStatus httpStatus, String errorMessage) {
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

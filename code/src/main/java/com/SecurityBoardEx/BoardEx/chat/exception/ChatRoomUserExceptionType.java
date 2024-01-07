package com.SecurityBoardEx.BoardEx.chat.exception;

import com.SecurityBoardEx.BoardEx.exception.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ChatRoomUserExceptionType implements BaseExceptionType {

    // 이 부분 변경
    NOT_FOUND_ROOMUSER(901,HttpStatus.NOT_FOUND, "찾으시는 인원이 없습니다");

    private int errorCode;
    private HttpStatus httpStatus;
    private String errorMessage;

    ChatRoomUserExceptionType(int errorCode, HttpStatus httpStatus, String errorMessage) {
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

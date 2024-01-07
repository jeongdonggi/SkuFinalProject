package com.SecurityBoardEx.BoardEx.board.exception;

import com.SecurityBoardEx.BoardEx.exception.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum BoardExceptionType implements BaseExceptionType {

    BOARD_NOT_FOUND(700, HttpStatus.NOT_FOUND, "찾으시는 글이 없습니다"),
    NOT_AUTHORITY_UPDATE_BOARD(701, HttpStatus.FORBIDDEN, "글을 업데이트할 권한이 없습니다."),
    NOT_AUTHORITY_DELETE_BOARD(702, HttpStatus.FORBIDDEN, "글을 삭제할 권한이 없습니다.");

    private int errorCode;
    private HttpStatus httpStatus;
    private String errorMessage;

    BoardExceptionType(int errorCode, HttpStatus httpStatus, String errorMessage) {
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

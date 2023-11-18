package com.SecurityBoardEx.BoardEx.file.exception;

import com.SecurityBoardEx.BoardEx.exception.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum FileExceptionType implements BaseExceptionType {

    FILE_CAN_NOT_SAVE(10000, HttpStatus.BAD_REQUEST, "파일 저장에 실패했습니다."),
    FILE_CAN_NOT_DELETE(10001, HttpStatus.BAD_REQUEST, "파일 삭제에 실패했습니다."),

    FILE_RESTRICTED(10002, HttpStatus.FORBIDDEN, "해당 파일 확장자는 업로드가 제한됩니다.");


    private int errorCode;
    private HttpStatus httpStatus;
    private String errorMessage;

    FileExceptionType(int errorCode, HttpStatus httpStatus, String errorMessage) {
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

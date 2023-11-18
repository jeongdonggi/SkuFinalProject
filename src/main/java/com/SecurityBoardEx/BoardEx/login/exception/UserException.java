package com.SecurityBoardEx.BoardEx.login.exception;

import com.SecurityBoardEx.BoardEx.exception.BaseException;
import com.SecurityBoardEx.BoardEx.exception.BaseExceptionType;

public class UserException extends BaseException {
    private BaseExceptionType exceptionType;


    // 생성자를 통해 생성하는 순간 ExceptionType을 정해주도록 한다.
    public UserException(BaseExceptionType exceptionType){
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType getExceptionType() {
        return exceptionType;
    }
}

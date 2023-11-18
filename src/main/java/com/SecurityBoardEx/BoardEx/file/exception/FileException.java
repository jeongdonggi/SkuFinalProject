package com.SecurityBoardEx.BoardEx.file.exception;

import com.SecurityBoardEx.BoardEx.exception.BaseException;
import com.SecurityBoardEx.BoardEx.exception.BaseExceptionType;

public class FileException extends BaseException {

    private BaseExceptionType exceptionType;

    public FileException(BaseExceptionType exceptionType){
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType getExceptionType(){
        return exceptionType;
    }
}

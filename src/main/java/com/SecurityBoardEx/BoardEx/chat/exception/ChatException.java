package com.SecurityBoardEx.BoardEx.chat.exception;

import com.SecurityBoardEx.BoardEx.exception.BaseException;
import com.SecurityBoardEx.BoardEx.exception.BaseExceptionType;

public class ChatException extends BaseException {
    private BaseExceptionType baseExceptionType;

    public ChatException(BaseExceptionType baseExceptionType){
        this.baseExceptionType = baseExceptionType;
    }

    @Override
    public BaseExceptionType getExceptionType(){
        return this.baseExceptionType;
    }
}

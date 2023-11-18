package com.SecurityBoardEx.BoardEx.chat.exception;

import com.SecurityBoardEx.BoardEx.exception.BaseException;
import com.SecurityBoardEx.BoardEx.exception.BaseExceptionType;

public class ChatRoomUserException extends BaseException {
    private BaseExceptionType baseExceptionType;

    public ChatRoomUserException(BaseExceptionType baseExceptionType){
        this.baseExceptionType = baseExceptionType;
    }

    @Override
    public BaseExceptionType getExceptionType(){
        return this.baseExceptionType;
    }
}

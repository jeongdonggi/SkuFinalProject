package com.SecurityBoardEx.BoardEx.comment.exception;

import com.SecurityBoardEx.BoardEx.exception.BaseException;
import com.SecurityBoardEx.BoardEx.exception.BaseExceptionType;

public class CommentException extends BaseException {

    private BaseExceptionType baseExceptionType;

    public CommentException(BaseExceptionType baseExceptionType){
        this.baseExceptionType = baseExceptionType;
    }

    @Override
    public BaseExceptionType getExceptionType(){
        return this.baseExceptionType;
    }
}

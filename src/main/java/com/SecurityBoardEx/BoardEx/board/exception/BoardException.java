package com.SecurityBoardEx.BoardEx.board.exception;

import com.SecurityBoardEx.BoardEx.exception.BaseException;
import com.SecurityBoardEx.BoardEx.exception.BaseExceptionType;

public class BoardException extends BaseException {

    private BaseExceptionType baseExceptionType;

    public BoardException(BaseExceptionType baseExceptionType){
        this.baseExceptionType = baseExceptionType;
    }

    @Override
    public BaseExceptionType getExceptionType() {
        return this.baseExceptionType;
    }
}

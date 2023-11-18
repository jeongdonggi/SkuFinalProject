package com.SecurityBoardEx.BoardEx.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.BindException;

// 컨트롤러 에러 상황에서 사용할 에러 예외 처리기
@ControllerAdvice
@Slf4j
public class ExceptionControllerAdvice {

    @ExceptionHandler(BaseException.class)
    public String handleBaseEx(BaseException exception, Model model){
        log.error("BaseException errorMessage() : {}" , exception.getExceptionType().getErrorMessage());
        log.error("BaseException errorCode() : {}", exception.getExceptionType().getErrorCode());

        model.addAttribute("errorCode", exception.getExceptionType().getErrorCode());
        model.addAttribute("errorMessage", exception.getExceptionType().getErrorMessage());
        model.addAttribute("errorStatus", exception.getExceptionType().getHttpStatus());

        return "exception/error";
    }

    //@Vaild에서 예외 발생
    @ExceptionHandler(BindException.class)
    public ResponseEntity handleValidEx(BindException exception){
        log.error("@ValidException 발생 : {}" ,  exception.getMessage());

        return new ResponseEntity(new ExceptionDto(2000),HttpStatus.BAD_REQUEST);
    }

    // HttpMessageNotReadableException => json 파싱 오류
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity httpMessageNotReadableExceptionEx(HttpMessageNotReadableException exception){
        log.error("Json 파싱 과정 예외 발생 : {}", exception.getMessage());
        return new ResponseEntity(new ExceptionDto(3000),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleUserEx(Exception exception){
        exception.printStackTrace();
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @Getter
    @ToString
    @AllArgsConstructor
    static class ExceptionDto{
        private Integer errorCode;
    }
}

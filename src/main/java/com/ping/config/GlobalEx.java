package com.ping.config;

import com.ping.utils.ResultT;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalEx {
    @ExceptionHandler(Exception.class)
    public ResultT<String> handleException(Exception e){
        return ResultT.error(e.getMessage());
    }
}

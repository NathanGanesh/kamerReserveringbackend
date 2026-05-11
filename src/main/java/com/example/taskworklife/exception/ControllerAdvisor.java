package com.example.taskworklife.exception;

import com.example.taskworklife.exception.kamer.KamerNotFoundException;
import com.example.taskworklife.models.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {


    HttpResponse baseResponse;

    @ExceptionHandler({javax.validation.ConstraintViolationException.class, KamerNotFoundException.class})
    public ResponseEntity<HttpResponse> inputValidationException(Exception e) {
        baseResponse.setMessage("Invalid Input : " + e.getMessage());
        return new ResponseEntity<HttpResponse>(baseResponse, HttpStatus.BAD_REQUEST);
    }
}

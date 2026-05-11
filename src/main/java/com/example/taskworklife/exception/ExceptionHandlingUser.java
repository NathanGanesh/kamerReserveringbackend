package com.example.taskworklife.exception;

import com.example.taskworklife.exception.user.*;
import com.example.taskworklife.models.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class ExceptionHandlingUser extends CreateResponse  implements ErrorController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(EmailExistException.class)
    public ResponseEntity<HttpResponse> emailExistException(EmailExistException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @RequestMapping("/error")
    public ResponseEntity<HttpResponse> notFound404() {
        return createHttpResponse(NOT_FOUND, "There is no mapping for this URL");
    }

    @ExceptionHandler(TermsNotAcceptedException.class)
    public ResponseEntity<HttpResponse> termsNotAcceptedException(TermsNotAcceptedException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<HttpResponse> emailNotFoundException(EmailNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NaamNotExistException.class)
    public ResponseEntity<HttpResponse> naamNotExistException(NaamNotExistException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(AchterNaamNotExistException.class)
    public ResponseEntity<HttpResponse> achterNaamNotExistException(AchterNaamNotExistException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NaamTeKleinException.class)
    public ResponseEntity<HttpResponse> naamTeKleinException(NaamTeKleinException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(EmailNotValidException.class)
    public ResponseEntity<HttpResponse> emailNotValidException(EmailNotValidException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }




}

package com.example.taskworklife.exception;

import com.example.taskworklife.exception.global.ImageException;
import com.example.taskworklife.exception.images.ImageTypeNotAllowedException;
import com.example.taskworklife.exception.images.ImagesExceededLimit;
import com.example.taskworklife.exception.images.ImagesNotFoundException;
import com.example.taskworklife.exception.reservering.ReservationAccessDeniedException;
import com.example.taskworklife.exception.reservering.ReserveringNotFoundException;
import com.example.taskworklife.exception.user.UserNotFoundException;
import com.example.taskworklife.models.HttpResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler extends CreateResponse {

    @ExceptionHandler(ImageException.class)
    public ResponseEntity<HttpResponse> imageException(ImageException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ImageTypeNotAllowedException.class)
    public ResponseEntity<HttpResponse> imageTypeNotAllowedException(ImageException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ImagesExceededLimit.class)
    public ResponseEntity<HttpResponse> imagesExceededLimit(ImageException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ImagesNotFoundException.class)
    public ResponseEntity<HttpResponse> imagesNotFoundException(ImagesNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpResponse> userNotFoundException(UserNotFoundException exception) {
        return createHttpResponse(NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ReserveringNotFoundException.class)
    public ResponseEntity<HttpResponse> reserveringNotFoundException(ReserveringNotFoundException exception) {
        return createHttpResponse(NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ReservationAccessDeniedException.class)
    public ResponseEntity<HttpResponse> reservationAccessDeniedException(ReservationAccessDeniedException exception) {
        return createHttpResponse(FORBIDDEN, exception.getMessage());
    }
}

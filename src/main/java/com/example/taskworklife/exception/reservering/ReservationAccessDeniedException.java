package com.example.taskworklife.exception.reservering;

public class ReservationAccessDeniedException extends RuntimeException {
    public ReservationAccessDeniedException(String message) {
        super(message);
    }
}

package com.example.taskworklife.models.pojo;

import java.time.LocalDateTime;

public class ReservationPojo {
    private LocalDateTime end;
    private LocalDateTime start;

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public ReservationPojo(LocalDateTime end, LocalDateTime start) {
        this.end = end;
        this.start = start;
    }

    public ReservationPojo() {
    }
}

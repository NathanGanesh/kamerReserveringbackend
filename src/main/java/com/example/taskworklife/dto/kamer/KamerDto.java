package com.example.taskworklife.dto.kamer;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
@Setter
public class KamerDto {
    private String naam;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime sluit;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime start;
}

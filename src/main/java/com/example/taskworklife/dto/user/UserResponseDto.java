package com.example.taskworklife.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserResponseDto {
    private Long id;
    private String naam;
    private String achternaam;
    private String email;
    private String profileImageUrl;
    private Date laatstIngelodgeDatumDisplay;
    private Date joinDate;
    private String role;
    private boolean active;
    private boolean notLocked;
}

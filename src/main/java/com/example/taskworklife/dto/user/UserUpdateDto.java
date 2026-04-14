package com.example.taskworklife.dto.user;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class UserUpdateDto {
    @NotNull(message = "{reservering.constraints.username.NotNull.message}")
    private String naam;

    @NotNull
    private String achternaam;

    @NotNull
    @Pattern(regexp = "\\w+@\\w+\\.\\w+(,\\s*\\w+@\\w+\\.\\w+)*", message = "not valid email")
    private String email;

    private String profileImageUrl;
    private boolean active;
    private boolean notLocked;
}

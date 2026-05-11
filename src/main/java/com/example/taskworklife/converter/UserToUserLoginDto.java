package com.example.taskworklife.converter;

import com.example.taskworklife.dto.user.UserLoginResponseDto;
import com.example.taskworklife.models.user.User;
import lombok.Synchronized;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class UserToUserLoginDto  implements Converter<User, UserLoginResponseDto> {


    @Synchronized
    @Nullable
    @Override
    public UserLoginResponseDto convert(User source) {
        UserLoginResponseDto userLoginDto = new UserLoginResponseDto();

        userLoginDto.setNaam(source.getNaam());
        userLoginDto.setAchternaam(source.getAchternaam());
        userLoginDto.setEmail(source.getEmail());
        userLoginDto.setProfileImageUrl(source.getProfileImageUrl());
        userLoginDto.setLaatstIngelodgeDatumDisplay(source.getLaatstIngelodgeDatumDisplay());
        userLoginDto.setRole(source.getRole());
        return userLoginDto;
    }
}

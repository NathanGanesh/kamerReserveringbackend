package com.example.taskworklife.converter;

import com.example.taskworklife.dto.user.UserResponseDto;
import com.example.taskworklife.models.user.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class UserToUserResponseDto implements Converter<User, UserResponseDto> {
    @Nullable
    @Override
    public UserResponseDto convert(User source) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(source.getId());
        dto.setNaam(source.getNaam());
        dto.setAchternaam(source.getAchternaam());
        dto.setEmail(source.getEmail());
        dto.setProfileImageUrl(source.getProfileImageUrl());
        dto.setLaatstIngelodgeDatumDisplay(source.getLaatstIngelodgeDatumDisplay());
        dto.setJoinDate(source.getJoinDate());
        dto.setRole(source.getRole());
        dto.setActive(source.isActive());
        dto.setNotLocked(source.isNotLocked());
        return dto;
    }
}

package com.example.taskworklife.service.user;

import com.example.taskworklife.dto.user.UserRegisterDto;
import com.example.taskworklife.dto.user.UserResponseDto;
import com.example.taskworklife.dto.user.UserUpdateDto;
import com.example.taskworklife.exception.user.EmailExistException;
import com.example.taskworklife.exception.user.EmailNotFoundException;
import com.example.taskworklife.exception.user.UserNotFoundException;
import com.example.taskworklife.models.user.User;

import java.util.List;

public interface UserService {
    User register(UserRegisterDto userRegisterDto) throws EmailExistException;

    List<UserResponseDto> getUsers();

    UserResponseDto getUserById(Long id) throws UserNotFoundException;

    UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto) throws UserNotFoundException, EmailExistException;

    void deleteUser(Long id) throws UserNotFoundException;

    User findUserByEmail(String email) throws EmailNotFoundException;
}

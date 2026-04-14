package com.example.taskworklife.controller;

import com.example.taskworklife.converter.UserToUserLoginDto;
import com.example.taskworklife.dto.user.UserLoginDto;
import com.example.taskworklife.dto.user.UserLoginResponseDto;
import com.example.taskworklife.dto.user.UserRegisterDto;
import com.example.taskworklife.dto.user.UserResponseDto;
import com.example.taskworklife.dto.user.UserUpdateDto;
import com.example.taskworklife.exception.ExceptionHandlingUser;
import com.example.taskworklife.exception.user.EmailExistException;
import com.example.taskworklife.exception.user.EmailNotFoundException;
import com.example.taskworklife.models.user.User;
import com.example.taskworklife.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "/user")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController extends ExceptionHandlingUser {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserToUserLoginDto userLoginResponseDtoConverter;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, UserToUserLoginDto userLoginResponseDtoConverter) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userLoginResponseDtoConverter = userLoginResponseDtoConverter;
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> login(@Valid @RequestBody UserLoginDto userLoginDto) throws EmailNotFoundException {
        authenticate(userLoginDto.getEmail(), userLoginDto.getWachtwoord());
        User loginUser = userService.findUserByEmail(userLoginDto.getEmail());
        UserLoginResponseDto loginConvertedUserDto = userLoginResponseDtoConverter.convert(loginUser);
        return new ResponseEntity<>(loginConvertedUserDto, OK);
    }

    @PostMapping("/register")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<UserLoginResponseDto> register(@Valid @RequestBody UserRegisterDto userRegisterDto) throws EmailExistException {
        User registeredUser = userService.register(userRegisterDto);
        UserLoginResponseDto registeredConvertedUserDto = userLoginResponseDtoConverter.convert(registeredUser);
        return new ResponseEntity<>(registeredConvertedUserDto, OK);
    }

    @GetMapping("/all")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return new ResponseEntity<>(userService.getUsers(), OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return new ResponseEntity<>(userService.getUserById(id), OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto userUpdateDto) throws EmailExistException {
        return new ResponseEntity<>(userService.updateUser(id, userUpdateDto), OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(NO_CONTENT);
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}

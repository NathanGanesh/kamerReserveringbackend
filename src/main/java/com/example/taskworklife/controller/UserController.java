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
import com.example.taskworklife.models.user.UserPrincipal;
import com.example.taskworklife.service.user.UserService;
import com.example.taskworklife.util.JWTTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = {"/user", "/users"})
@CrossOrigin(origins = "http://localhost:3000")
public class UserController extends ExceptionHandlingUser {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserToUserLoginDto userLoginResponseDtoConverter;
    private final JWTTokenProvider jwtTokenProvider;

    @Autowired
    public UserController(
            UserService userService,
            AuthenticationManager authenticationManager,
            UserToUserLoginDto userLoginResponseDtoConverter,
            JWTTokenProvider jwtTokenProvider
    ) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userLoginResponseDtoConverter = userLoginResponseDtoConverter;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> login(@Valid @RequestBody UserLoginDto userLoginDto) throws EmailNotFoundException {
        Authentication authentication = authenticate(userLoginDto.getEmail(), userLoginDto.getWachtwoord());
        return new ResponseEntity<>(toAuthenticatedResponse(authentication), OK);
    }

    @PostMapping({"", "/register"})
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<UserLoginResponseDto> register(@Valid @RequestBody UserRegisterDto userRegisterDto) throws EmailExistException {
        User registeredUser = userService.register(userRegisterDto);
        return new ResponseEntity<>(toAuthenticatedResponse(registeredUser), OK);
    }

    @GetMapping({"", "/all"})
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

    private Authentication authenticate(String username, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    private UserLoginResponseDto toAuthenticatedResponse(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            throw new AuthenticationCredentialsNotFoundException("Authenticated user principal missing");
        }

        return toAuthenticatedResponse(((UserPrincipal) principal).getUser());
    }

    private UserLoginResponseDto toAuthenticatedResponse(User user) {
        UserLoginResponseDto responseDto = userLoginResponseDtoConverter.convert(user);
        responseDto.setToken(jwtTokenProvider.generateJwtToken(new UserPrincipal(user)));
        responseDto.setTokenType("Bearer");
        return responseDto;
    }
}

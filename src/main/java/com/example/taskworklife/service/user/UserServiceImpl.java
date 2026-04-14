package com.example.taskworklife.service.user;

import com.example.taskworklife.converter.UserRegisterDtoToUser;
import com.example.taskworklife.converter.UserToUserResponseDto;
import com.example.taskworklife.dto.user.UserRegisterDto;
import com.example.taskworklife.dto.user.UserResponseDto;
import com.example.taskworklife.dto.user.UserUpdateDto;
import com.example.taskworklife.exception.user.EmailExistException;
import com.example.taskworklife.exception.user.EmailNotFoundException;
import com.example.taskworklife.exception.user.UserNotFoundException;
import com.example.taskworklife.models.user.User;
import com.example.taskworklife.models.user.UserPrincipal;
import com.example.taskworklife.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final UserRepo userRepository;
    private final UserRegisterDtoToUser userRegisterDtoToUserConverter;
    private final UserToUserResponseDto userResponseDtoConverter;

    @Autowired
    public UserServiceImpl(UserRepo userRepository, UserRegisterDtoToUser userRegisterDtoToUserConverter, UserToUserResponseDto userResponseDtoConverter) {
        this.userRepository = userRepository;
        this.userRegisterDtoToUserConverter = userRegisterDtoToUserConverter;
        this.userResponseDtoConverter = userResponseDtoConverter;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User userByEmail = userRepository.findUserByEmail(email);
        if (userByEmail == null) {
            LOGGER.error("No user found with " + email);
            throw new UsernameNotFoundException("Not found!");
        }

        userByEmail.setLaatstIngelodgeDatumDisplay(userByEmail.getLaatstIngelodgeDatum());
        userByEmail.setLaatstIngelodgeDatum(new Date());
        userRepository.save(userByEmail);
        LOGGER.info("Returning found user by username: " + email);
        return new UserPrincipal(userByEmail);
    }

    @Override
    public User register(UserRegisterDto userRegisterDto) throws EmailExistException {
        User userByEmail = userRepository.findUserByEmail(userRegisterDto.getEmail());
        if (userByEmail != null) {
            LOGGER.error("User already found with email: " + userRegisterDto.getEmail());
            throw new EmailExistException("User already found with email: " + userRegisterDto.getEmail());
        }

        User createdUser = userRegisterDtoToUserConverter.convert(userRegisterDto);
        LOGGER.info("Nieuwe gebruiker met email: " + userRegisterDto.getEmail());
        assert createdUser != null;
        return userRepository.save(createdUser);
    }

    @Override
    public List<UserResponseDto> getUsers() {
        return userRepository.findAll().stream()
                .map(userResponseDtoConverter::convert)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto getUserById(Long id) throws UserNotFoundException {
        return userResponseDtoConverter.convert(getUserEntityById(id));
    }

    @Override
    public UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto) throws UserNotFoundException, EmailExistException {
        User existingUser = getUserEntityById(id);

        if (!existingUser.getEmail().equalsIgnoreCase(userUpdateDto.getEmail()) && userRepository.existsByEmail(userUpdateDto.getEmail())) {
            throw new EmailExistException("User already found with email: " + userUpdateDto.getEmail());
        }

        existingUser.setNaam(userUpdateDto.getNaam());
        existingUser.setAchternaam(userUpdateDto.getAchternaam());
        existingUser.setEmail(userUpdateDto.getEmail());
        existingUser.setProfileImageUrl(userUpdateDto.getProfileImageUrl());
        existingUser.setActive(userUpdateDto.isActive());
        existingUser.setNotLocked(userUpdateDto.isNotLocked());

        return userResponseDtoConverter.convert(userRepository.save(existingUser));
    }

    @Override
    public void deleteUser(Long id) throws UserNotFoundException {
        userRepository.delete(getUserEntityById(id));
    }

    @Override
    public User findUserByEmail(String email) throws EmailNotFoundException {
        User userByEmail = userRepository.findUserByEmail(email);
        if (userByEmail == null) {
            LOGGER.error("User not found with email: " + email);
            throw new EmailNotFoundException("Email not found");
        }
        return userByEmail;
    }

    private User getUserEntityById(Long id) throws UserNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User niet gevonden"));
    }
}

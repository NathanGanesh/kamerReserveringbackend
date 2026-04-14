package com.example.taskworklife.converter;

import com.example.taskworklife.dto.user.UserRegisterDto;
import com.example.taskworklife.exception.user.EmailNotValidException;
import com.example.taskworklife.exception.user.NaamNotExistException;
import com.example.taskworklife.exception.user.NaamTeKleinException;
import com.example.taskworklife.exception.user.TermsNotAcceptedException;
import com.example.taskworklife.models.user.User;
import lombok.SneakyThrows;
import lombok.Synchronized;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.taskworklife.enumeration.Role.ROLE_USER;

@Component
public class UserRegisterDtoToUser implements Converter<UserRegisterDto, User> {
    private static final String EMAIL_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);

    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserRegisterDtoToUser(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @SneakyThrows
    @Synchronized
    @Nullable
    @Override
    public User convert(UserRegisterDto source) {
        final User user = new User();
        if (!source.isTerms()) {
            throw new TermsNotAcceptedException("Terms arent accepted");
        }

        user.setWachtwoord(passwordEncoder.encode(source.getWachtwoord()));

        if (!StringUtils.isNotBlank(source.getNaam())) {
            throw new NaamNotExistException("Naam bestaat niet");
        }
        if (source.getNaam().length() < 3) {
            throw new NaamTeKleinException("Naam is te klein");
        }
        user.setNaam(source.getNaam());

        if (!StringUtils.isNotBlank(source.getAchterNaam())) {
            throw new NaamNotExistException("Achternaam bestaat niet");
        }
        if (source.getAchterNaam().length() < 3) {
            throw new NaamTeKleinException("Achternaam is te klein");
        }
        user.setAchternaam(source.getAchterNaam());

        if (!isValid(source.getEmail())) {
            throw new EmailNotValidException("Email is niet juist");
        }

        user.setEmail(source.getEmail());
        user.setJoinDate(new Date());
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(ROLE_USER.name());
        user.setAuthorities(Arrays.asList(ROLE_USER.getAuthorities()));
        return user;
    }

    public static boolean isValid(final String email) {
        Matcher matcher = PATTERN.matcher(email);
        return matcher.matches();
    }
}

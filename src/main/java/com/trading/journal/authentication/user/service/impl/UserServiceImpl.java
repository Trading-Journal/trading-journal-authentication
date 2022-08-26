package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.password.service.PasswordService;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.user.service.UserService;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import com.trading.journal.authentication.verification.properties.VerificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    public static final String USER_NOT_FOUND = "User not found";
    private final UserRepository userRepository;

    private final UserAuthorityService userAuthorityService;

    private final VerificationProperties verificationProperties;

    private final PasswordService passwordService;

    @Override
    public Optional<User> getUserByEmail(@NotBlank String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User createNewUser(@NotNull UserRegistration userRegistration, Tenancy tenancy) {
        Boolean validUser = validateNewUser(userRegistration.getUserName(), userRegistration.getEmail());
        if (validUser) {
            User user = userRepository.save(buildUser(userRegistration, tenancy));
            List<UserAuthority> userAuthorities = userAuthorityService.saveCommonUserAuthorities(user);
            user.setAuthorities(userAuthorities);
            return user;
        } else {
            throw new ApplicationException("User name or email already exist");
        }
    }

    @Override
    public Boolean validateNewUser(@NotNull String userName, @NotBlank String email) {
        Boolean userNameExists = userNameExists(userName);
        Boolean emailExists = emailExists(email);
        return !userNameExists && !emailExists;
    }

    @Override
    public Boolean userNameExists(@NotBlank String userName) {
        return userRepository.existsByUserName(userName);
    }

    @Override
    public Boolean emailExists(@NotBlank String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void verifyUser(@NotBlank String email) {
        User user = this.getUserByEmail(email)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND));
        user.enable();
        user.verify();
        userRepository.save(user);
    }

    @Override
    public void unprovenUser(String email) {
        User user = this.getUserByEmail(email)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND));
        user.unproven();
        userRepository.save(user);
    }


    @Override
    public User changePassword(@NotBlank String email, @NotBlank String password) {
        User user = this.getUserByEmail(email)
                .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND));
        user.changePassword(passwordService.encodePassword(password));
        return userRepository.save(user);
    }

    @Override
    public Boolean existsByTenancyId(Long tenancyId) {
        return userRepository.existsByTenancyId(tenancyId);
    }

    private User buildUser(UserRegistration userRegistration, Tenancy tenancy) {
        boolean enabledAndVerified = !verificationProperties.isEnabled();
        return User.builder()
                .tenancy(tenancy)
                .userName(userRegistration.getUserName())
                .password(passwordService.encodePassword(userRegistration.getPassword()))
                .firstName(userRegistration.getFirstName())
                .lastName(userRegistration.getLastName())
                .email(userRegistration.getEmail())
                .enabled(enabledAndVerified)
                .verified(enabledAndVerified)
                .createdAt(LocalDateTime.now())
                .build();
    }
}

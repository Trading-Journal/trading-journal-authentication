package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.password.service.PasswordService;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import com.trading.journal.authentication.verification.properties.VerificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApplicationUserServiceImpl implements ApplicationUserService {

    private final ApplicationUserRepository applicationUserRepository;

    private final UserAuthorityService userAuthorityService;

    private final VerificationProperties verificationProperties;

    private final PasswordService passwordService;

    @Override
    public ApplicationUser getUserByEmail(@NotBlank String email) {
        return applicationUserRepository.findByEmail(email)
                .map(applicationUser -> {
                    applicationUser.loadAuthorities(userAuthorityService.getByUserId(applicationUser.getId()));
                    return applicationUser;
                })
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User %s does not exist", email)));
    }

    @Override
    public ApplicationUser createNewUser(@NotNull UserRegistration userRegistration) {
        Boolean validUser = validateNewUser(userRegistration.userName(), userRegistration.email());
        if (validUser) {
            ApplicationUser applicationUser = applicationUserRepository.save(user(userRegistration));
            userAuthorityService.saveCommonUserAuthorities(applicationUser);
            return applicationUser;
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
        return applicationUserRepository.existsByUserName(userName);
    }

    @Override
    public Boolean emailExists(@NotBlank String email) {
        return applicationUserRepository.existsByEmail(email);
    }

    @Override
    public UserInfo getUserInfo(@NotBlank String email) {
        ApplicationUser applicationUser = this.getUserByEmail(email);
        return new UserInfo(applicationUser);
    }

    @Override
    public void verifyNewUser(@NotBlank String email) {
        ApplicationUser applicationUser = this.getUserByEmail(email);
        applicationUser.enable();
        applicationUser.verify();
        applicationUserRepository.save(applicationUser);
    }

    @Override
    public ApplicationUser changePassword(@NotBlank String email, @NotBlank String password) {
        ApplicationUser applicationUser = this.getUserByEmail(email);
        applicationUser.changePassword(passwordService.encodePassword(password));
        return applicationUserRepository.save(applicationUser);
    }

    private ApplicationUser user(UserRegistration userRegistration) {
        boolean enabledAndVerified = !verificationProperties.isEnabled();
        return ApplicationUser.builder()
                .userName(userRegistration.userName())
                .password(passwordService.encodePassword(userRegistration.password()))
                .firstName(userRegistration.firstName())
                .lastName(userRegistration.lastName())
                .email(userRegistration.email())
                .enabled(enabledAndVerified)
                .verified(enabledAndVerified)
                .createdAt(LocalDateTime.now())
                .build();
    }
}

package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.password.service.PasswordService;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import com.trading.journal.authentication.verification.properties.VerificationProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(SpringExtension.class)
public class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserAuthorityService userAuthorityService;

    @Mock
    PasswordService passwordService;

    @Mock
    VerificationProperties verificationProperties;

    @InjectMocks
    UserServiceImpl applicationUserServiceImpl;

    @Test
    @DisplayName("When create user and the verification is disabled return user response enabled and verified")
    void createUser() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();

        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userAuthorityService.saveCommonUserAuthorities(any())).thenReturn(singletonList(new UserAuthority(applicationUser, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))));
        when(passwordService.encodePassword(anyString())).thenReturn("sdsa54ds56a4ds564d");
        when(userRepository.save(any())).thenReturn(applicationUser);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(applicationUser));
        when(verificationProperties.isEnabled()).thenReturn(false);

        User newUser = applicationUserServiceImpl.createNewUser(userRegistration, null);
        assertThat(newUser.getEnabled()).isTrue();
        assertThat(newUser.getVerified()).isTrue();
    }

    @Test
    @DisplayName("When create user and the verification is enabled return user response disabled and not verified")
    void createUserDisabled() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(false)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userAuthorityService.saveCommonUserAuthorities(any())).thenReturn(singletonList(new UserAuthority(applicationUser, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))));
        when(passwordService.encodePassword(anyString())).thenReturn("sdsa54ds56a4ds564d");
        when(userRepository.save(any())).thenReturn(applicationUser);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(applicationUser));
        when(verificationProperties.isEnabled()).thenReturn(true);

        User newUser = applicationUserServiceImpl.createNewUser(userRegistration, null);
        assertThat(newUser.getEnabled()).isFalse();
        assertThat(newUser.getVerified()).isFalse();
    }

    @Test
    @DisplayName("Enable and verify user")
    void enableAndVerify() {
        User disabledUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(false)
                .verified(false)
                .createdAt(LocalDateTime.of(2022, 2, 1, 10, 30, 50))
                .authorities(emptyList())
                .build();

        User enabledUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.of(2022, 2, 1, 10, 30, 50))
                .authorities(emptyList())
                .build();


        when(userRepository.findByEmail(disabledUser.getEmail())).thenReturn(Optional.of(disabledUser));
        when(userRepository.save(enabledUser)).thenReturn(enabledUser);

        applicationUserServiceImpl.verifyUser(disabledUser.getEmail());
    }

    @Test
    @DisplayName("Enable and verify user that does not exist, return an exception")
    void enableAndVerifyException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(ApplicationException.class, () -> applicationUserServiceImpl.verifyUser("mail@mail.com"), "User not found");
    }

    @Test
    @DisplayName("Make user unverified by unproven it")
    void unprovenUser() {
        User verifiedUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.of(2022, 2, 1, 10, 30, 50))
                .authorities(emptyList())
                .build();

        User unprovenUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(false)
                .createdAt(LocalDateTime.of(2022, 2, 1, 10, 30, 50))
                .authorities(emptyList())
                .build();

        when(userRepository.findByEmail(verifiedUser.getEmail())).thenReturn(Optional.of(verifiedUser));
        when(userRepository.save(unprovenUser)).thenReturn(unprovenUser);

        applicationUserServiceImpl.unprovenUser(verifiedUser.getEmail());
    }

    @Test
    @DisplayName("Unproven user that does not exist, return an exception")
    void unprovenUserException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(ApplicationException.class, () -> applicationUserServiceImpl.unprovenUser("mail@mail.com"), "User not found");
    }

    @Test
    @DisplayName("When create user and user name already exist return exception")
    void userNameAlreadyExist() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        when(userRepository.existsByUserName(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> applicationUserServiceImpl.createNewUser(userRegistration, null));
        assertThat(exception.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("User name or email already exist");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("When create user and email already exist return exception")
    void emailAlreadyExist() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> applicationUserServiceImpl.createNewUser(userRegistration, null));
        assertThat(exception.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("User name or email already exist");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("When create user and user name and email already exist return exception")
    void userNameAndEmailAlreadyExist() {
        UserRegistration userRegistration = new UserRegistration(
                null,
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        when(userRepository.existsByUserName(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> applicationUserServiceImpl.createNewUser(userRegistration, null));
        assertThat(exception.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("User name or email already exist");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("When validate new user with valid data return is valid")
    void isValid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        Boolean userValid = applicationUserServiceImpl.validateNewUser(userName, email);
        assertThat(userValid).isTrue();
    }

    @Test
    @DisplayName("When validate new user with invalid data return is invalid")
    void isInvalid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(userRepository.existsByUserName(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        Boolean userValid = applicationUserServiceImpl.validateNewUser(userName, email);
        assertThat(userValid).isFalse();
    }

    @Test
    @DisplayName("When validate new user with valid user name and invalid email return is valid")
    void isEmailInvalid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        Boolean userValid = applicationUserServiceImpl.validateNewUser(userName, email);
        assertThat(userValid).isFalse();
    }

    @Test
    @DisplayName("When validate new user with invalid user name and valid email return is valid")
    void isUserNameInvalid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(userRepository.existsByUserName(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        Boolean userValid = applicationUserServiceImpl.validateNewUser(userName, email);
        assertThat(userValid).isFalse();
    }

    @Test
    @DisplayName("Change password")
    void changePassword() {
        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.of(2022, 2, 1, 10, 30, 50))
                .authorities(emptyList())
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(applicationUser));
        when(passwordService.encodePassword("password")).thenReturn("new_password_encoded");

        User userWithNewPassword = User.builder()
                .id(1L)
                .userName("UserName")
                .password("new_password_encoded")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.of(2022, 2, 1, 10, 30, 50))
                .authorities(emptyList())
                .build();
        when(userRepository.save(userWithNewPassword)).thenReturn(userWithNewPassword);

        applicationUserServiceImpl.changePassword("mail@mail.com", "password");
    }

    @Test
    @DisplayName("Change password when user email does not exist return exception")
    void changePasswordException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(ApplicationException.class, () -> applicationUserServiceImpl.changePassword("mail@mail.com", "password"), "User not found");
    }
}

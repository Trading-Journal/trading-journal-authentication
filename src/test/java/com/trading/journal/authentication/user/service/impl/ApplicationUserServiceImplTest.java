package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.password.service.PasswordService;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import com.trading.journal.authentication.verification.properties.VerificationProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(SpringExtension.class)
public class ApplicationUserServiceImplTest {

    @Mock
    ApplicationUserRepository applicationUserRepository;

    @Mock
    UserAuthorityService userAuthorityService;

    @Mock
    PasswordService passwordService;

    @Mock
    VerificationProperties verificationProperties;

    @InjectMocks
    ApplicationUserServiceImpl applicationUserServiceImpl;

    @Test
    @DisplayName("When create user and the verification is disabled return user response enabled and verified")
    void createUser() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        ApplicationUser applicationUser = ApplicationUser.builder()
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

        when(applicationUserRepository.existsByUserName(anyString())).thenReturn(false);
        when(applicationUserRepository.existsByEmail(anyString())).thenReturn(false);
        when(userAuthorityService.saveCommonUserAuthorities(any())).thenReturn(singletonList(new UserAuthority(applicationUser, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))));
        when(passwordService.encodePassword(anyString())).thenReturn("sdsa54ds56a4ds564d");
        when(applicationUserRepository.save(any())).thenReturn(applicationUser);
        when(applicationUserRepository.findById(anyLong())).thenReturn(Optional.of(applicationUser));
        when(verificationProperties.isEnabled()).thenReturn(false);

        ApplicationUser newUser = applicationUserServiceImpl.createNewUser(userRegistration);
        assertThat(newUser.getEnabled()).isTrue();
        assertThat(newUser.getVerified()).isTrue();
    }

    @Test
    @DisplayName("When create user and the verification is enabled return user response disabled and not verified")
    void createUserDisabled() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        ApplicationUser applicationUser = ApplicationUser.builder()
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

        when(applicationUserRepository.existsByEmail(anyString())).thenReturn(false);
        when(applicationUserRepository.existsByUserName(anyString())).thenReturn(false);
        when(userAuthorityService.saveCommonUserAuthorities(any())).thenReturn(singletonList(new UserAuthority(applicationUser, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))));
        when(passwordService.encodePassword(anyString())).thenReturn("sdsa54ds56a4ds564d");
        when(applicationUserRepository.save(any())).thenReturn(applicationUser);
        when(applicationUserRepository.findById(anyLong())).thenReturn(Optional.of(applicationUser));
        when(verificationProperties.isEnabled()).thenReturn(true);

        ApplicationUser newUser = applicationUserServiceImpl.createNewUser(userRegistration);
        assertThat(newUser.getEnabled()).isFalse();
        assertThat(newUser.getVerified()).isFalse();
    }

    @Test
    @DisplayName("Enable and verify user")
    void enableAndVerify() {
        ApplicationUser disabledUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(false)
                .verified(false)
                .createdAt( LocalDateTime.of(2022, 2, 1, 10, 30, 50))
                .authorities(emptyList())
                .build();

        ApplicationUser enabledUser =  ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt( LocalDateTime.of(2022, 2, 1, 10, 30, 50))
                .authorities(emptyList())
                .build();


        when(applicationUserRepository.findByEmail(disabledUser.getEmail())).thenReturn(Optional.of(disabledUser));
        when(applicationUserRepository.save(enabledUser)).thenReturn(enabledUser);

        applicationUserServiceImpl.verifyUser(disabledUser.getEmail());
    }

    @Test
    @DisplayName("Enable and verify user that does not exist, return an exception")
    void enableAndVerifyException() {
        when(applicationUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> applicationUserServiceImpl.verifyUser("mail@mail.com"), "User mail@mail.com does not exist");
    }

    @Test
    @DisplayName("Make user unverified by unproven it")
    void unprovenUser() {
        ApplicationUser verifiedUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt( LocalDateTime.of(2022, 2, 1, 10, 30, 50))
                .authorities(emptyList())
                .build();

        ApplicationUser unprovenUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(false)
                .createdAt( LocalDateTime.of(2022, 2, 1, 10, 30, 50))
                .authorities(emptyList())
                .build();

        when(applicationUserRepository.findByEmail(verifiedUser.getEmail())).thenReturn(Optional.of(verifiedUser));
        when(applicationUserRepository.save(unprovenUser)).thenReturn(unprovenUser);

        applicationUserServiceImpl.unprovenUser(verifiedUser.getEmail());
    }

    @Test
    @DisplayName("Unproven user that does not exist, return an exception")
    void unprovenUserException() {
        when(applicationUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> applicationUserServiceImpl.unprovenUser("mail@mail.com"), "User mail@mail.com does not exist");
    }

    @Test
    @DisplayName("When create user and user name already exist return exception")
    void userNameAlreadyExist() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        when(applicationUserRepository.existsByUserName(anyString())).thenReturn(true);
        when(applicationUserRepository.existsByEmail(anyString())).thenReturn(false);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> applicationUserServiceImpl.createNewUser(userRegistration));
        assertThat(exception.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("User name or email already exist");

        verify(applicationUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("When create user and email already exist return exception")
    void emailAlreadyExist() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        when(applicationUserRepository.existsByUserName(anyString())).thenReturn(false);
        when(applicationUserRepository.existsByEmail(anyString())).thenReturn(true);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> applicationUserServiceImpl.createNewUser(userRegistration));
        assertThat(exception.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("User name or email already exist");

        verify(applicationUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("When create user and user name and email already exist return exception")
    void userNameAndEmailAlreadyExist() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        when(applicationUserRepository.existsByUserName(anyString())).thenReturn(true);
        when(applicationUserRepository.existsByEmail(anyString())).thenReturn(true);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> applicationUserServiceImpl.createNewUser(userRegistration));
        assertThat(exception.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("User name or email already exist");

        verify(applicationUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("When validate new user with valid data return is valid")
    void isValid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(applicationUserRepository.existsByUserName(anyString())).thenReturn(false);
        when(applicationUserRepository.existsByEmail(anyString())).thenReturn(false);

        Boolean userValid = applicationUserServiceImpl.validateNewUser(userName, email);
        assertThat(userValid).isTrue();
    }

    @Test
    @DisplayName("When validate new user with invalid data return is invalid")
    void isInvalid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(applicationUserRepository.existsByUserName(anyString())).thenReturn(true);
        when(applicationUserRepository.existsByEmail(anyString())).thenReturn(false);

        Boolean userValid = applicationUserServiceImpl.validateNewUser(userName, email);
        assertThat(userValid).isFalse();
    }

    @Test
    @DisplayName("When validate new user with valid user name and invalid email return is valid")
    void isEmailInvalid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(applicationUserRepository.existsByUserName(anyString())).thenReturn(false);
        when(applicationUserRepository.existsByEmail(anyString())).thenReturn(true);

        Boolean userValid = applicationUserServiceImpl.validateNewUser(userName, email);
        assertThat(userValid).isFalse();
    }

    @Test
    @DisplayName("When validate new user with invalid user name and valid email return is valid")
    void isUserNameInvalid() {
        String userName = "user";
        String email = "mail@mail.com";

        when(applicationUserRepository.existsByUserName(anyString())).thenReturn(true);
        when(applicationUserRepository.existsByEmail(anyString())).thenReturn(true);

        Boolean userValid = applicationUserServiceImpl.validateNewUser(userName, email);
        assertThat(userValid).isFalse();
    }

    @Test
    @DisplayName("Change password")
    void changePassword() {
        ApplicationUser applicationUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt( LocalDateTime.of(2022, 2, 1, 10, 30, 50))
                .authorities(emptyList())
                .build();

        when(applicationUserRepository.findByEmail(anyString())).thenReturn(Optional.of(applicationUser));
        when(passwordService.encodePassword("password")).thenReturn("new_password_encoded");

        ApplicationUser applicationUserWithNewPassword = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("new_password_encoded")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt( LocalDateTime.of(2022, 2, 1, 10, 30, 50))
                .authorities(emptyList())
                .build();
        when(applicationUserRepository.save(applicationUserWithNewPassword)).thenReturn(applicationUserWithNewPassword);

        applicationUserServiceImpl.changePassword("mail@mail.com", "password");
    }

    @Test
    @DisplayName("Change password when user email does not exist return exception")
    void changePasswordException() {
        when(applicationUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> applicationUserServiceImpl.changePassword("mail@mail.com", "password"), "User email@mail.com does not exist");
    }

    @Test
    @DisplayName("Given an email load user info")
    void userInfo() {
        ApplicationUser applicationUser = ApplicationUser.builder()
                .id(1L)
                .userName("UserName")
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.of(2022, 2, 1, 10, 30, 50))
                .authorities(asList(
                        new UserAuthority(new ApplicationUser(), new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")),
                        new UserAuthority(new ApplicationUser(), new Authority(2L, AuthorityCategory.ADMINISTRATOR, "ROLE_ADMIN"))
                ))
                .build();

        when(applicationUserRepository.findByEmail("mail@mail.com")).thenReturn(Optional.of(applicationUser));

        UserInfo info = applicationUserServiceImpl.getUserInfo("mail@mail.com");
        assertThat(info.getId()).isEqualTo(1L);
        assertThat(info.getUserName()).isEqualTo("UserName");
        assertThat(info.getFirstName()).isEqualTo("firstName");
        assertThat(info.getLastName()).isEqualTo("lastName");
        assertThat(info.getEmail()).isEqualTo("mail@mail.com");
        assertThat(info.getEnabled()).isEqualTo(true);
        assertThat(info.getVerified()).isEqualTo(true);
        assertThat(info.getAuthorities()).containsExactly("ROLE_USER", "ROLE_ADMIN");
    }
}

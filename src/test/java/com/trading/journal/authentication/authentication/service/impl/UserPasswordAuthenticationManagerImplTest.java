package com.trading.journal.authentication.authentication.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.jwt.data.ContextUser;
import com.trading.journal.authentication.password.service.PasswordService;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import com.trading.journal.authentication.userauthority.UserAuthority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UserPasswordAuthenticationManagerImplTest {

    @Mock
    ApplicationUserRepository applicationUserRepository;

    @Mock
    PasswordService passwordService;

    @InjectMocks
    UserPasswordAuthenticationManagerImpl authenticationManager;

    @DisplayName("Given correct authentication return authenticated user")
    @Test
    void authenticated() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("mail@mail.com", "raw_password");

        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserAdm",
                "encoded_password",
                "user",
                "admin",
                "mail@mail.com",
                true,
                true,
                singletonList(new UserAuthority(null, "ROLE_USER", 1L)),
                LocalDateTime.now());
        when(applicationUserRepository.findByEmail("mail@mail.com")).thenReturn(Optional.of(applicationUser));
        when(passwordService.matches("raw_password", "encoded_password")).thenReturn(true);

        Authentication authenticated = authenticationManager.authenticate(authenticationToken);
        ContextUser principal = (ContextUser) authenticated.getPrincipal();
        assertThat(principal.email()).isEqualTo("mail@mail.com");
        assertThat(principal.tenancy()).isEqualTo("UserAdm");
        assertThat(authenticated.getCredentials()).isNull();
        assertThat(authenticated.getAuthorities()).hasSize(1);
        assertThat(authenticated.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_USER");
    }

    @DisplayName("User not found by Principal/Email return 401 Bad Credentials")
    @Test
    void userNotFound() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("mail@mail.com", "raw_password");
        when(applicationUserRepository.findByEmail("mail@mail.com")).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authenticationManager.authenticate(authenticationToken));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("Bad Credentials");

        verify(passwordService, never()).matches(anyString(), anyString());
    }

    @DisplayName("User found by Principal/Email but not enabled return 401 Locked Credentials")
    @Test
    void userDisabled() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("mail@mail.com", "raw_password");

        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserAdm",
                "encoded_password",
                "user",
                "admin",
                "mail@mail.com",
                false,
                false,
                emptyList(),
                LocalDateTime.now());
        when(applicationUserRepository.findByEmail("mail@mail.com")).thenReturn(Optional.of(applicationUser));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authenticationManager.authenticate(authenticationToken));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("Locked Credentials");

        verify(passwordService, never()).matches(anyString(), anyString());
    }

    @DisplayName("Passwords do not match return 401 Bad Credentials")
    @Test
    void noMatchPassword() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("mail@mail.com", "raw_password");

        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserAdm",
                "encoded_password",
                "user",
                "admin",
                "mail@mail.com",
                true,
                true,
                emptyList(),
                LocalDateTime.now());
        when(applicationUserRepository.findByEmail("mail@mail.com")).thenReturn(Optional.of(applicationUser));
        when(passwordService.matches("raw_password", "encoded_password")).thenReturn(false);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authenticationManager.authenticate(authenticationToken));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("Bad Credentials");
    }

    @DisplayName("None Authorities found for the user return 401 Authorities")
    @Test
    void emptyAuthorities() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("mail@mail.com", "raw_password");

        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserAdm",
                "encoded_password",
                "user",
                "admin",
                "mail@mail.com",
                true,
                true,
                emptyList(),
                LocalDateTime.now());
        when(applicationUserRepository.findByEmail("mail@mail.com")).thenReturn(Optional.of(applicationUser));
        when(passwordService.matches("raw_password", "encoded_password")).thenReturn(true);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authenticationManager.authenticate(authenticationToken));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("No Authorities");
    }

    @DisplayName("None Authorities found for the user return 401 Authorities")
    @Test
    void nullAuthorities() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("mail@mail.com", "raw_password");

        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "UserAdm",
                "encoded_password",
                "user",
                "admin",
                "mail@mail.com",
                true,
                true,
                null,
                LocalDateTime.now());
        when(applicationUserRepository.findByEmail("mail@mail.com")).thenReturn(Optional.of(applicationUser));
        when(passwordService.matches("raw_password", "encoded_password")).thenReturn(true);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authenticationManager.authenticate(authenticationToken));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("No Authorities");
    }
}
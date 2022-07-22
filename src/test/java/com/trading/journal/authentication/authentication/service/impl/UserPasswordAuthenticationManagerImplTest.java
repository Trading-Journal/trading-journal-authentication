package com.trading.journal.authentication.authentication.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.jwt.data.ContextUser;
import com.trading.journal.authentication.password.service.PasswordService;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
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
    UserRepository userRepository;

    @Mock
    PasswordService passwordService;

    @InjectMocks
    UserPasswordAuthenticationManagerImpl authenticationManager;

    @DisplayName("Given correct authentication return authenticated user")
    @Test
    void authenticated() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("mail@mail.com", "raw_password");
        User applicationUser = User.builder()
                .id(1L)
                .userName("UserAdm")
                .password("encoded_password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(singletonList(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))))
                .tenancy(Tenancy.builder().name("UserAdm").build())
                .build();
        when(userRepository.findByEmail("mail@mail.com")).thenReturn(Optional.of(applicationUser));
        when(passwordService.matches("raw_password", "encoded_password")).thenReturn(true);

        Authentication authenticated = authenticationManager.authenticate(authenticationToken);
        ContextUser principal = (ContextUser) authenticated.getPrincipal();
        assertThat(principal.email()).isEqualTo("mail@mail.com");
        assertThat(principal.tenancyName()).isEqualTo("UserAdm");
        assertThat(authenticated.getCredentials()).isNull();
        assertThat(authenticated.getAuthorities()).hasSize(1);
        assertThat(authenticated.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_USER");
    }

    @DisplayName("User not found by Principal/Email return 401 Bad Credentials")
    @Test
    void userNotFound() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("mail@mail.com", "raw_password");
        when(userRepository.findByEmail("mail@mail.com")).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authenticationManager.authenticate(authenticationToken));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("Bad Credentials");

        verify(passwordService, never()).matches(anyString(), anyString());
    }

    @DisplayName("User found by Principal/Email but not enabled return 401 Locked Credentials")
    @Test
    void userDisabled() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("mail@mail.com", "raw_password");

        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("12345679")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(false)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();
        when(userRepository.findByEmail("mail@mail.com")).thenReturn(Optional.of(applicationUser));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authenticationManager.authenticate(authenticationToken));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("Locked Credentials");

        verify(passwordService, never()).matches(anyString(), anyString());
    }

    @DisplayName("User found by Principal/Email but not verified return 401 Locked Credentials")
    @Test
    void userUnverified() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("mail@mail.com", "raw_password");

        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("12345679")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();
        when(userRepository.findByEmail("mail@mail.com")).thenReturn(Optional.of(applicationUser));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authenticationManager.authenticate(authenticationToken));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("Locked Credentials");

        verify(passwordService, never()).matches(anyString(), anyString());
    }

    @DisplayName("Passwords do not match return 401 Bad Credentials")
    @Test
    void noMatchPassword() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("mail@mail.com", "raw_password");

        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("encoded_password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();
        when(userRepository.findByEmail("mail@mail.com")).thenReturn(Optional.of(applicationUser));
        when(passwordService.matches("raw_password", "encoded_password")).thenReturn(false);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authenticationManager.authenticate(authenticationToken));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("Bad Credentials");
    }

    @DisplayName("None Authorities found for the user return 401 Authorities")
    @Test
    void emptyAuthorities() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("mail@mail.com", "raw_password");

        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("encoded_password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();
        when(userRepository.findByEmail("mail@mail.com")).thenReturn(Optional.of(applicationUser));
        when(passwordService.matches("raw_password", "encoded_password")).thenReturn(true);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authenticationManager.authenticate(authenticationToken));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("No Authorities");
    }

    @DisplayName("None Authorities found for the user return 401 Authorities")
    @Test
    void nullAuthorities() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("mail@mail.com", "raw_password");

        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("encoded_password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(null)
                .build();
        when(userRepository.findByEmail("mail@mail.com")).thenReturn(Optional.of(applicationUser));
        when(passwordService.matches("raw_password", "encoded_password")).thenReturn(true);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authenticationManager.authenticate(authenticationToken));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getStatusText()).isEqualTo("No Authorities");
    }

    @DisplayName("If tenancy is not enabled then return exception")
    @Test
    void tenancyNotEnable() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("mail@mail.com", "raw_password");
        User applicationUser = User.builder()
                .id(1L)
                .userName("UserAdm")
                .password("encoded_password")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(singletonList(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))))
                .tenancy(Tenancy.builder().name("UserAdm").enabled(false).build())
                .build();
        when(userRepository.findByEmail("mail@mail.com")).thenReturn(Optional.of(applicationUser));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> authenticationManager.authenticate(authenticationToken));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exception.getStatusText()).isEqualTo("Your tenancy is disabled by the system admin");

        verify(passwordService, never()).matches(anyString(), anyString());
    }
}
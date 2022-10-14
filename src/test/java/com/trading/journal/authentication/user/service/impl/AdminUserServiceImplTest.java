package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.password.service.PasswordService;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AdminUserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserAuthorityService userAuthorityService;

    @Mock
    VerificationService verificationService;

    @Mock
    PasswordService passwordService;

    @InjectMocks
    AdminUserServiceImpl applicationAdminUserService;

    @DisplayName("When count one user admin true")
    @Test
    void thereIsAdmin() {
        List<String> roles = singletonList("ROLE_ADMIN");

        when(userRepository.countAdmins(roles)).thenReturn(1);

        Boolean thereIsAdmin = applicationAdminUserService.thereIsAdmin();
        assertThat(thereIsAdmin).isTrue();
    }

    @DisplayName("When count zero user admin false")
    @Test
    void thereIsNoAdmin() {
        List<String> roles = singletonList("ROLE_ADMIN");

        when(userRepository.countAdmins(roles)).thenReturn(0);

        Boolean thereIsAdmin = applicationAdminUserService.thereIsAdmin();
        assertThat(thereIsAdmin).isFalse();
    }

    @DisplayName("Given admin registration create admin user and send the verification email")
    @Test
    void createAdmin() {
        UserRegistration adminRegistration = new UserRegistration(null,"john", "rambo", "admin", "mail@mail.com", null, null, false);

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

        UserAuthority userAuthority = new UserAuthority(applicationUser, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"));

        when(passwordService.randomPassword()).thenReturn("password_secret");
        when(userRepository.save(any())).thenReturn(applicationUser);
        when(userAuthorityService.saveAdminUserAuthorities(applicationUser)).thenReturn(singletonList(userAuthority));
        when(userRepository.findById(1L)).thenReturn(Optional.of(applicationUser));
        doNothing().when(verificationService).send(VerificationType.ADMIN_REGISTRATION, applicationUser);

        applicationAdminUserService.createAdmin(adminRegistration);
    }
}
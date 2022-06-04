package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.authority.UserAuthority;
import com.trading.journal.authentication.authority.service.UserAuthorityService;
import com.trading.journal.authentication.registration.AdminRegistration;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.service.ApplicationUserRepository;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ApplicationAdminUserServiceImplTest {

    @Mock
    ApplicationUserRepository applicationUserRepository;

    @Mock
    UserAuthorityService userAuthorityService;

    @Mock
    VerificationService verificationService;

    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    ApplicationAdminUserServiceImpl applicationAdminUserService;

    @DisplayName("When count one user admin true")
    @Test
    void thereIsAdmin() {
        List<String> roles = singletonList("ROLE_ADMIN");

        when(applicationUserRepository.countAdmins(roles)).thenReturn(Mono.just(1));

        Mono<Boolean> booleanMono = applicationAdminUserService.thereIsAdmin();

        StepVerifier.create(booleanMono)
                .expectNext(true)
                .verifyComplete();
    }

    @DisplayName("When count zero user admin false")
    @Test
    void thereIsNoAdmin() {
        List<String> roles = singletonList("ROLE_ADMIN");

        when(applicationUserRepository.countAdmins(roles)).thenReturn(Mono.just(0));

        Mono<Boolean> booleanMono = applicationAdminUserService.thereIsAdmin();

        StepVerifier.create(booleanMono)
                .expectNext(false)
                .verifyComplete();
    }

    @DisplayName("Given admin registration create admin user and send the verification email")
    @Test
    void createAdmin() {
        AdminRegistration adminRegistration = new AdminRegistration("john", "rambo", "admin", "mail@mail.com");

        ApplicationUser applicationUser = new ApplicationUser(
                1L,
                "admin",
                "password_secret",
                "john",
                "rambo",
                "mail@mail.com",
                false,
                false,
                Collections.singletonList(new UserAuthority(1L, 1L, 1L, "ROLE_USER")),
                LocalDateTime.now());

        UserAuthority userAuthority = new UserAuthority(1L, "ADMIN", 1L);

        when(encoder.encode("mail@mail.com")).thenReturn("password_secret");
        when(applicationUserRepository.save(any())).thenReturn(Mono.just(applicationUser));
        when(userAuthorityService.saveAdminUserAuthorities(applicationUser)).thenReturn(Mono.just(userAuthority));
        when(applicationUserRepository.findById(1L)).thenReturn(Mono.just(applicationUser));
        when(verificationService.send(VerificationType.ADMIN_REGISTRATION, applicationUser)).thenReturn(Mono.empty());

        Mono<Void> voidMono = applicationAdminUserService.createAdmin(adminRegistration);
        StepVerifier.create(voidMono)
                .expectNextCount(0)
                .verifyComplete();
    }
}
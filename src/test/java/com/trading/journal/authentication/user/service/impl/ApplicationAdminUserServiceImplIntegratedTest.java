package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.authority.UserAuthority;
import com.trading.journal.authentication.authority.service.UserAuthorityRepository;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.properties.AdminUserProperties;
import com.trading.journal.authentication.user.service.ApplicationAdminUserService;
import com.trading.journal.authentication.user.service.ApplicationUserRepository;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
import com.trading.journal.authentication.verification.service.VerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
@TestPropertySource(properties = {"journal.authentication.authority.type=DATABASE"})
class ApplicationAdminUserServiceImplIntegratedTest {

    @Autowired
    ApplicationUserRepository applicationUserRepository;

    @Autowired
    VerificationRepository verificationRepository;

    @Autowired
    UserAuthorityRepository userAuthorityRepository;

    @Autowired
    AdminUserProperties adminUserProperties;

    @Autowired
    ApplicationAdminUserService applicationAdminUserService;

    @MockBean
    VerificationEmailService verificationEmailService;

    @BeforeEach
    public void setUp() {
        applicationUserRepository.deleteAll().block();
        verificationRepository.deleteAll().block();
        userAuthorityRepository.deleteAll().block();
    }

    @Test
    void addAdmin() {
        when(verificationEmailService.sendEmail(any(), any())).thenReturn(Mono.empty());

        UserRegistration adminRegistration = new UserRegistration("Admin", "Administrator", "admin", adminUserProperties.email(), null, null);

        Mono<Boolean> thereIsAdmin = applicationAdminUserService.thereIsAdmin();
        StepVerifier.create(thereIsAdmin)
                .expectNext(false)
                .verifyComplete();

        Mono<Void> admin = applicationAdminUserService.createAdmin(adminRegistration);
        StepVerifier.create(admin)
                .expectNextCount(0)
                .verifyComplete();

        AtomicReference<Long> userId = new AtomicReference<>();
        Mono<UserInfo> userInfo = applicationUserRepository.findByUserName("admin");
        StepVerifier.create(userInfo)
                .assertNext(user -> {
                    assertThat(user.getEnabled()).isFalse();
                    assertThat(user.getVerified()).isFalse();
                    userId.set(user.getId());
                })
                .verifyComplete();

        Flux<UserAuthority> userAuthorityFlux = userAuthorityRepository.findByUserId(userId.get());
        StepVerifier.create(userAuthorityFlux)
                .expectNextCount(2)
                .verifyComplete();

        List<UserAuthority> userAuthorities = userAuthorityFlux.collectList().block();
        assert userAuthorities != null;
        assertThat(userAuthorities).extracting(UserAuthority::getName).containsAnyOf("ROLE_USER", "ROLE_ADMIN");

        Mono<Verification> verificationMono = verificationRepository.getByTypeAndEmail(VerificationType.ADMIN_REGISTRATION, adminUserProperties.email());
        StepVerifier.create(verificationMono)
                .assertNext(verification -> assertThat(verification.getHash()).isNotBlank())
                .verifyComplete();
    }
}
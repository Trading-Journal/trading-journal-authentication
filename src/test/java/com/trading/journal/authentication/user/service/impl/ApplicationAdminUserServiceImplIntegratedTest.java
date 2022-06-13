package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.UserAuthorityRepository;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.properties.AdminUserProperties;
import com.trading.journal.authentication.user.service.ApplicationAdminUserService;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
import com.trading.journal.authentication.verification.VerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
@TestPropertySource(properties = {"journal.authentication.authority.type=DATABASE", "journal.authentication.admin-user.email=admin@email.com"})
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
        applicationUserRepository.deleteAll();
        verificationRepository.deleteAll();
        userAuthorityRepository.deleteAll();
        doNothing().when(verificationEmailService).sendEmail(any(), any());
    }

    @Test
    void addAdmin() {
        UserRegistration adminRegistration = new UserRegistration("Admin", "Administrator", "admin", adminUserProperties.email(), null, null);

        Boolean thereIsAdmin = applicationAdminUserService.thereIsAdmin();
        assertThat(thereIsAdmin).isFalse();

        applicationAdminUserService.createAdmin(adminRegistration);

        Long userId;
        ApplicationUser applicationUser = applicationUserRepository.findByEmail(adminUserProperties.email());
        assertThat(applicationUser.getEnabled()).isFalse();
        assertThat(applicationUser.getVerified()).isFalse();
        userId = applicationUser.getId();

        List<UserAuthority> userAuthorities = userAuthorityRepository.findByUserId(userId);
        assertThat(userAuthorities).hasSize(2);
        assertThat(userAuthorities).extracting(UserAuthority::getName).containsAnyOf("ROLE_USER", "ROLE_ADMIN");

        Verification verification = verificationRepository.getByTypeAndEmail(VerificationType.ADMIN_REGISTRATION, adminUserProperties.email());
        assertThat(verification.getHash()).isNotBlank();
    }
}
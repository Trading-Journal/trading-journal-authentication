package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.MySqlTestContainerInitializer;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.tenancy.TenancyRepository;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
import com.trading.journal.authentication.user.properties.AdminUserProperties;
import com.trading.journal.authentication.user.service.AdminUserService;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.UserAuthorityRepository;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationRepository;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = MySqlTestContainerInitializer.class)
@TestPropertySource(properties = {"journal.authentication.admin-user.email=admin@email.com", "journal.authentication.verification.enabled=true"})
class AdminUserServiceImplIntegratedTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    VerificationRepository verificationRepository;

    @Autowired
    UserAuthorityRepository userAuthorityRepository;

    @Autowired
    AdminUserProperties adminUserProperties;

    @Autowired
    AdminUserService adminUserService;

    @MockBean
    VerificationEmailService verificationEmailService;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        verificationRepository.deleteAll();
        userAuthorityRepository.deleteAll();
        doNothing().when(verificationEmailService).sendEmail(any(), any());
    }

    @AfterAll
    public static void shutdown(@Autowired UserRepository userRepository){
        userRepository.deleteAll();
    }

    @Test
    void addAdmin() {
        UserRegistration adminRegistration = new UserRegistration(null,"Admin", "Administrator", "admin", adminUserProperties.email(), null, null);

        Boolean thereIsAdmin = adminUserService.thereIsAdmin();
        assertThat(thereIsAdmin).isFalse();

        adminUserService.createAdmin(adminRegistration);

        Long userId;
        User applicationUser = userRepository.findByEmail(adminUserProperties.email()).get();
        assertThat(applicationUser.getEnabled()).isFalse();
        assertThat(applicationUser.getVerified()).isFalse();
        userId = applicationUser.getId();

        assertThat(applicationUser.getAuthorities()).hasSize(3);
        assertThat(applicationUser.getAuthorities()).extracting(UserAuthority::getAuthority).extracting(Authority::getName).containsAnyOf("ROLE_USER", "ROLE_ADMIN", "TENANCY_ADMIN");

        Verification verification = verificationRepository.getByTypeAndEmail(VerificationType.ADMIN_REGISTRATION, adminUserProperties.email()).get();
        assertThat(verification.getHash()).isNotBlank();
    }
}
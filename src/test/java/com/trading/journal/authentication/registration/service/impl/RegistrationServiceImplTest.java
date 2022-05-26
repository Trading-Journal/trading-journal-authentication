package com.trading.journal.authentication.registration.service.impl;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;

import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserService;
import com.trading.journal.authentication.user.UserAuthority;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
public class RegistrationServiceImplTest {

    @Mock
    ApplicationUserService applicationUserService;

    @InjectMocks
    RegistrationServiceImpl registrationService;

    @Test
    @DisplayName("Happy flow dor user registration")
    void testHappyFlow() {
        UserRegistration userRegistration = new UserRegistration(
                "firstName",
                "lastName",
                "UserName",
                "mail@mail.com",
                "123456",
                "123456");

        ApplicationUser appUser = new ApplicationUser(
                1L,
                "UserName",
                "sdsa54ds56a4ds564d",
                "firstName",
                "lastName",
                "mail@mail.com",
                true,
                true,
                Collections.singletonList(new UserAuthority(1L, 1L, "ROLE_USER")),
                LocalDateTime.now());

        when(applicationUserService.createNewUser(userRegistration)).thenReturn(Mono.just(appUser));

        Mono<Void> voidMono = registrationService.signUp(userRegistration);

        StepVerifier.create(voidMono).verifyComplete();
    }
}

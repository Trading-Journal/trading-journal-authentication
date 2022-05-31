package com.trading.journal.authentication.registration.service.impl;

import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.registration.service.RegistrationService;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final ApplicationUserService applicationUserService;

    @Override
    public Mono<Void> signUp(@Valid UserRegistration userRegistration) {
        return applicationUserService.createNewUser(userRegistration).then().name("signup_user").metrics();
    }

}

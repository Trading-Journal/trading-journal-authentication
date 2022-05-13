package com.trading.journal.authentication.registration.service.impl;

import javax.validation.Valid;

import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.registration.service.RegistrationService;
import com.trading.journal.authentication.user.ApplicationUserService;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final ApplicationUserService applicationUserService;

    public RegistrationServiceImpl(ApplicationUserService applicationUserService) {
        this.applicationUserService = applicationUserService;
    }

    @Override
    public Mono<Void> signUp(@Valid UserRegistration userRegistration) {
        return applicationUserService.createNewUser(userRegistration).then().name("signup").metrics();
    }

}

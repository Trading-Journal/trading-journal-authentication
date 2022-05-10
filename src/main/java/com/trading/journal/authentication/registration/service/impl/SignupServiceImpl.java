package com.trading.journal.authentication.registration.service.impl;

import javax.validation.Valid;

import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.registration.service.SignupService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class SignupServiceImpl implements SignupService {

    private final Logger logger = LoggerFactory.getLogger(SignupServiceImpl.class);

    @Override
    public Mono<Void> signUp(@Valid UserRegistration userRegistration) {
        logger.info("The user registration is: {}", userRegistration);
        return Mono.empty();
    }

}

package com.trading.journal.authentication.verification.impl;

import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class VerificationEmailServiceImpl implements VerificationEmailService {


    @Override
    public Mono<Void> sendEmail(Verification verification, ApplicationUser applicationUser) {
        return null;
    }
}

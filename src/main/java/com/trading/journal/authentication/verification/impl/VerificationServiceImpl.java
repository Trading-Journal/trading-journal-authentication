package com.trading.journal.authentication.verification.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.verification.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationRepository verificationRepository;
    private final VerificationEmailService verificationEmailService;

    @Override
    public Mono<Void> send(VerificationType verificationType, ApplicationUser applicationUser) {
        return verificationRepository.getByTypeAndEmail(verificationType, applicationUser.getEmail())
                .map(verificationRepository::delete)
                .then(verificationRepository.save(create(verificationType, applicationUser)))
                .flatMap(verification -> verificationEmailService.sendEmail(verification, applicationUser));
    }

    @Override
    public Mono<Verification> retrieve(String hash, String email) {
        return verificationRepository.getByHashAndEmail(hash, email)
                .switchIfEmpty(Mono.error(new ApplicationException(HttpStatus.BAD_REQUEST, "Request is invalid")));
    }

    @Override
    public Mono<Void> verify(Verification verification) {
        return verificationRepository.delete(verification);
    }

    private Verification create(VerificationType verificationType, ApplicationUser applicationUser) {
        return Verification.builder()
                .email(applicationUser.getEmail())
                .hash("12456")
                .status(VerificationStatus.PENDING)
                .type(verificationType)
                .build();
    }
}

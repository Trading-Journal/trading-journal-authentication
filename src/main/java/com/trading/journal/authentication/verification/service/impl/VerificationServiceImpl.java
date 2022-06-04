package com.trading.journal.authentication.verification.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.service.ApplicationUserService;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.HashProvider;
import com.trading.journal.authentication.verification.service.VerificationEmailService;
import com.trading.journal.authentication.verification.service.VerificationRepository;
import com.trading.journal.authentication.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationRepository verificationRepository;
    private final VerificationEmailService verificationEmailService;

    private final ApplicationUserService applicationUserService;

    private final HashProvider hashProvider;

    @Override
    public Mono<Void> send(VerificationType verificationType, ApplicationUser applicationUser) {
        return verificationRepository.getByTypeAndEmail(verificationType, applicationUser.getEmail())
                .switchIfEmpty(create(verificationType, applicationUser))
                .map(verification -> verification.renew(hashProvider.generateHash(verification.getEmail())))
                .flatMap(verificationRepository::save)
                .flatMap(verification -> verificationEmailService.sendEmail(verification, applicationUser));
    }

    @Override
    public Mono<Verification> retrieve(String hash) {
        String email = hashProvider.readHashValue(hash);
        return verificationRepository.getByHashAndEmail(hash, email)
                .switchIfEmpty(Mono.error(new ApplicationException(HttpStatus.BAD_REQUEST, "Request is invalid")));
    }

    @Override
    public Mono<Void> verify(Verification verification) {
        Mono<Void> delete = verificationRepository.delete(verification);
        if(VerificationType.ADMIN_REGISTRATION.equals(verification.getType())){
            delete = applicationUserService.getUserByEmail(verification.getEmail())
                    .map(applicationUser -> this.send(VerificationType.CHANGE_PASSWORD, applicationUser))
                    .then(delete);
        }
        return delete;
    }

    private Mono<Verification> create(VerificationType verificationType, ApplicationUser applicationUser) {
        return Mono.just(Verification.builder()
                .email(applicationUser.getEmail())
                .type(verificationType)
                .build());
    }
}

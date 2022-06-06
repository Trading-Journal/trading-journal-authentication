package com.trading.journal.authentication.verification.service;

import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;

public interface VerificationService {

    void send(VerificationType verificationType, ApplicationUser applicationUser);

    Verification retrieve(String hash);

    void verify(Verification verification);
}

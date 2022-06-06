package com.trading.journal.authentication.verification.service;

import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.verification.Verification;

public interface VerificationEmailService {
    void sendEmail(Verification verification, ApplicationUser applicationUser);
}

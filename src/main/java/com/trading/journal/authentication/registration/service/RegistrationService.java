package com.trading.journal.authentication.registration.service;

import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;

public interface RegistrationService {
    SignUpResponse signUp(UserRegistration userRegistration);

    void verify(String hash);

    SignUpResponse sendVerification(String email);
}

package com.trading.journal.authentication.authentication.service;

import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;

public interface AuthenticationService {
    LoginResponse signIn(Login login);

    LoginResponse refreshToken(String refreshToken);
}

package com.trading.journal.authentication.jwt.service;

import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.user.User;

public interface JwtTokenProvider {

    TokenData generateAccessToken(User applicationUser);

    TokenData generateRefreshToken(User applicationUser);

    TokenData generateTemporaryToken(String email);
}

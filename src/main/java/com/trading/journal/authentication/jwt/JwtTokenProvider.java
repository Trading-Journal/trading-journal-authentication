package com.trading.journal.authentication.jwt;

import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.user.ApplicationUser;

public interface JwtTokenProvider {

    TokenData generateAccessToken(ApplicationUser applicationUser);

    TokenData generateRefreshToken(ApplicationUser applicationUser);
}

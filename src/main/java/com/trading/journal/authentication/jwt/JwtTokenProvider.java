package com.trading.journal.authentication.jwt;

import java.util.List;

import com.trading.journal.authentication.user.ApplicationUser;

import org.springframework.security.core.Authentication;

public interface JwtTokenProvider {

    TokenData generateJwtToken(ApplicationUser applicationUser);

    Authentication getAuthentication(String token);

    boolean validateToken(String token);

    List<String> getRoles(String token);
}

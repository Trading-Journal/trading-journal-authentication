package com.trading.journal.authentication.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public interface JwtTokenParser {
    Jws<Claims> parseToken(String token);
}

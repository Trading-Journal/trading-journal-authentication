package com.trading.journal.authentication.jwt;

import java.io.Serial;

public class JwtException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -6161720324171559483L;

    public JwtException(String message) {
        super(message);
    }

    public JwtException(String message, Exception e) {
        super(message, e);
    }
}
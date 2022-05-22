package com.trading.journal.authentication.jwt;

public class JwtException extends RuntimeException {
    private static final long serialVersionUID = -6161720324171559483L;

    public JwtException(String message) {
        super(message);
    }

    public JwtException(Exception e) {
        super(e);
    }

    public JwtException(String message, Exception e) {
        super(message, e);
    }
}
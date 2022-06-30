package com.trading.journal.authentication.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtExceptionTest {

    @DisplayName("Jwt exception with message")
    @Test
    void message() {
        JwtException exception = new JwtException("any message");
        assertThat(exception.getMessage()).isEqualTo("any message");
    }

    @DisplayName("Jwt exception with message and cause")
    @Test
    void messageAndCause() {
        NullPointerException nullPointerException = new NullPointerException("null value");
        JwtException exception = new JwtException("any message", nullPointerException);
        assertThat(exception.getMessage()).isEqualTo("any message");
        assertThat(exception).hasCauseInstanceOf(NullPointerException.class);
    }
}
package com.trading.journal.authentication.jwt.data;

import java.time.LocalDateTime;

public record TokenData(
        String token,
        LocalDateTime issuedAt) {
}
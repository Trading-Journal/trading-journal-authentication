package com.trading.journal.authentication.jwt.data;

import java.time.LocalDateTime;

public record TokenData(
        String accessToken,
        String refreshToken,
        LocalDateTime issuedAt) {
}
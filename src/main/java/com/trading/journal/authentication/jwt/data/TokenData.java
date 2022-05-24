package com.trading.journal.authentication.jwt.data;

import java.util.Date;

public record TokenData(
        String accessToken,
        String refreshToken,
        long expirationIn,
        Date issuedAt) {
}
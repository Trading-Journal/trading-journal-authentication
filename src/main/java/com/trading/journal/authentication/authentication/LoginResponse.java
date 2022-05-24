package com.trading.journal.authentication.authentication;

import java.util.Date;

public record LoginResponse(
        String type,

        String accessToken,

        String refreshToken,

        Long expirationInSeconds,

        Date issuedAt,

        String user) {
}

package com.trading.journal.authentication.authentication;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trading.journal.authentication.jwt.helper.DateHelper;

public record LoginResponse(
        String type,

        String accessToken,

        String refreshToken,

        @JsonFormat(pattern = DateHelper.DATE_TIME_FORMAT) LocalDateTime issuedAt,

        String user) {
}

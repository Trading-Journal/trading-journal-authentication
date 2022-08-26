package com.trading.journal.authentication.authentication;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trading.journal.authentication.helper.DateHelper;

import java.time.LocalDateTime;

public record LoginResponse(
        String type,
        String accessToken,
        String refreshToken,
        @JsonFormat(pattern = DateHelper.DATE_TIME_FORMAT) LocalDateTime issuedAt,
        String user) {
}

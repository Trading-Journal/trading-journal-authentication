package com.trading.journal.authentication.jwt.data;

import java.util.List;

public record AccessTokenInfo(
        String subject,
        Long tenancyId,
        String tenancyName,
        List<String> scopes) {
}

package com.trading.journal.authentication.jwt.data;

import java.util.List;

public record AccessTokenInfo(
        String userName,
        String tenancy,
        List<String> scopes) {
}

package com.trading.journal.authentication.jwt;

import java.util.List;

public record AccessTokenInfo(
        String userName,
        String tenancy,
        List<String> roles) {
}

package com.trading.journal.authentication.authentication;

import java.util.Date;
import java.util.List;

public record LoginResponse(
        String type,

        String token,

        List<String> roles,

        Long expirationIn,

        Date issuedAt,

        String userName,

        String user) {
}

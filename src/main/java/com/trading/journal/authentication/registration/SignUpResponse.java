package com.trading.journal.authentication.registration;

public record SignUpResponse (
        String email,
        boolean enabled
) {
}

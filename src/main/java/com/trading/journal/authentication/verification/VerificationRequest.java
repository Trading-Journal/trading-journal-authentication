package com.trading.journal.authentication.verification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VerificationRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email is invalid") @Size(max = 128, message = "Email size is invalid") String email,
        @NotNull(message = "Type is required") VerificationType verificationType
) {
}

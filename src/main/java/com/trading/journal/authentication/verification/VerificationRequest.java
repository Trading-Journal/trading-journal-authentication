package com.trading.journal.authentication.verification;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record VerificationRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email is invalid") @Size(max = 128, message = "Email size is invalid") String email,
        @NotNull(message = "Type is required") VerificationType verificationType
) {
}

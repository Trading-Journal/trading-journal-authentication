package com.trading.journal.authentication.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Login(
        @NotBlank(message = "Email is required") @Email(message = "Email is invalid") @Size(max = 128, message = "Email size is invalid") String email,
        @NotBlank(message = "Password is required") @Size(max = 128, min = 1, message = "Password size is invalid") String password) {
}

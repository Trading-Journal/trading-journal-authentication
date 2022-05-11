package com.trading.journal.authentication.authentication;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record Login(
        @NotBlank(message = "Email  is required") @Email(message = "Email is invalid") @Size(max = 128, message = "Email size is invalid") String email,

        @NotBlank(message = "Password  is required") @Size(max = 128, min = 10, message = "Password size is invalid") String password) {
}

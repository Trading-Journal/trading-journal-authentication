package com.trading.journal.authentication.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MeUpdate(
        @NotBlank(message = "User name is required") @Size(max = 128, min = 5, message = "User name size is invalid") String userName,

        @NotBlank(message = "First name is required") @Size(max = 128, min = 3, message = "First name size is invalid") String firstName,

        @NotBlank(message = "Last name is required") @Size(max = 128, min = 3, message = "Las name size is invalid") String lastName
) {
}

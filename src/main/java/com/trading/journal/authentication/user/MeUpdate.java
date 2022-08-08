package com.trading.journal.authentication.user;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record MeUpdate(
        @NotBlank(message = "User name is required") @Size(max = 128, min = 5, message = "User name size is invalid") String userName,

        @NotBlank(message = "First name is required") @Size(max = 128, min = 3, message = "First name size is invalid") String firstName,

        @NotBlank(message = "Last name is required") @Size(max = 128, min = 3, message = "Las name size is invalid") String lastName
) {
}

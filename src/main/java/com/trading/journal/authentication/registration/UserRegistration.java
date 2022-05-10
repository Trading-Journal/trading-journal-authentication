package com.trading.journal.authentication.registration;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.trading.journal.authentication.registration.validation.PasswordConfirmed;
import com.trading.journal.authentication.registration.validation.PasswordPolicy;

@PasswordConfirmed
public record UserRegistration(
                @NotBlank(message = "First name is required") @Size(max = 128, min = 3, message = "First name size is invalid") String firstName,

                @NotBlank(message = "Last name is required") @Size(max = 128, min = 3, message = "Las name size is invalid") String lastName,

                @NotBlank(message = "User name is required") @Size(max = 128, min = 5, message = "User name size is invalid") String userName,

                @NotBlank(message = "Email is required") @Email(message = "Email is invalid") @Size(max = 128, message = "Email size is invalid") String email,

                @NotBlank(message = "Password is required") @PasswordPolicy @Size(max = 128, min = 10, message = "Password size is invalid") String password,

                @NotBlank(message = "Password confirmation is required") @Size(max = 128, min = 10, message = "Password confirmation size is invalid") String confirmPassword) {
}

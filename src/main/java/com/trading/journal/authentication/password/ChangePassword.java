package com.trading.journal.authentication.password;

import com.trading.journal.authentication.password.validation.PasswordAndConfirmation;
import com.trading.journal.authentication.password.validation.PasswordConfirmed;
import com.trading.journal.authentication.password.validation.PasswordPolicy;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@PasswordConfirmed
public record ChangePassword(
        @NotBlank(message = "Email is required") @Email(message = "Email is invalid") @Size(max = 128, message = "Email size is invalid") String email,
        @NotBlank(message = "Hash is required") String hash,
        @NotBlank(message = "Password is required") @PasswordPolicy @Size(max = 128, min = 10, message = "Password size is invalid") String password,
        @NotBlank(message = "Password confirmation is required") @Size(max = 128, min = 10, message = "Password confirmation size is invalid") String confirmPassword)
    implements PasswordAndConfirmation
{
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getConfirmPassword() {
        return confirmPassword;
    }
}

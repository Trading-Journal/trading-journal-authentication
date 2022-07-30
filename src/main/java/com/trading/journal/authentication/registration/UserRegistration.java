package com.trading.journal.authentication.registration;

import com.trading.journal.authentication.password.validation.PasswordAndConfirmation;
import com.trading.journal.authentication.password.validation.PasswordConfirmed;
import com.trading.journal.authentication.password.validation.PasswordPolicy;
import lombok.*;
import org.springframework.util.StringUtils;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
@PasswordConfirmed
public class UserRegistration implements PasswordAndConfirmation {

    @Size(max = 254, message = "Company name size is invalid")
    private String companyName;

    @NotBlank(message = "First name is required")
    @Size(max = 128, min = 3, message = "First name size is invalid")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 128, min = 3, message = "Las name size is invalid")
    private String lastName;

    @NotBlank(message = "User name is required")
    @Size(max = 128, min = 5, message = "User name size is invalid")
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Size(max = 128, message = "Email size is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    @PasswordPolicy
    @Size(max = 128, min = 10, message = "Password size is invalid")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    @Size(max = 128, min = 10, message = "Password confirmation size is invalid")
    private String confirmPassword;

    public String getCompanyName() {
        String name = companyName;
        if (!StringUtils.hasText(name)) {
            name = userName;
        }
        return name;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void randomPassword() {
        String password = UUID.randomUUID().toString();
        this.password = password;
        this.confirmPassword = password;
    }
}

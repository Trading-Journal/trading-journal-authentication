package com.trading.journal.authentication.password.validation;

import java.util.Objects;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordPolicyValidator implements ConstraintValidator<PasswordPolicy, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        boolean isValid = false;
        if (Objects.nonNull(password)) {
            isValid = PasswordValidator.isValid(password);
        }
        return isValid;
    }
}

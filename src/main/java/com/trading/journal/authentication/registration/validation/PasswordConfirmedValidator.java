package com.trading.journal.authentication.registration.validation;

import java.util.Optional;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.trading.journal.authentication.registration.UserRegistration;

public class PasswordConfirmedValidator implements ConstraintValidator<PasswordConfirmed, UserRegistration> {

    @Override
    public boolean isValid(UserRegistration userRegistration, ConstraintValidatorContext context) {
        String password = Optional.ofNullable(userRegistration).map(UserRegistration::password).orElse("");
        String confirmedPassword = Optional.ofNullable(userRegistration).map(UserRegistration::confirmPassword)
                .orElse("");
        return password.equals(confirmedPassword);
    }
}

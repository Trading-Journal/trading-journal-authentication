package com.trading.journal.authentication.password.validation;

import java.util.Objects;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import lombok.RequiredArgsConstructor;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordPolicyValidator implements ConstraintValidator<PasswordPolicy, String> {

    private final PasswordValidator passwordValidator;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        boolean isValid = false;
        if (Objects.nonNull(password)) {
            RuleResult ruleResult = passwordValidator.validate(new PasswordData(password));
            if (!ruleResult.isValid()) {
                StringBuilder messages = new StringBuilder();
                for (String message : passwordValidator.getMessages(ruleResult)) {
                    messages.append(message).append(System.lineSeparator());
                }
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(messages.toString()).addConstraintViolation();
            }
            isValid = ruleResult.isValid();
        }
        return isValid;
    }
}

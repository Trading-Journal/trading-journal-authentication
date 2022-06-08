package com.trading.journal.authentication.password.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.validation.ConstraintValidatorContext;

import com.trading.journal.authentication.configuration.PasswordConfiguration;

import com.trading.journal.authentication.password.validation.PasswordPolicyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.passay.PasswordValidator;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class PasswordPolicyValidatorTest {

    final PasswordConfiguration passwordConfiguration = new PasswordConfiguration();

    ConstraintValidatorContext constraintContext;

    PasswordPolicyValidator passwordPolicyValidator;

    @BeforeEach
    public void setUp() {
        PasswordValidator passwordValidator = passwordConfiguration.getPasswordValidator();
        passwordPolicyValidator = new PasswordPolicyValidator(passwordValidator);

        constraintContext = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder = mock(
                ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(constraintContext.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(constraintViolationBuilder);
    }

    @Test
    @DisplayName("Password format is valid")
    void testValidPassword() {
        boolean valid = passwordPolicyValidator.isValid("&cLqQyN2CFlPi*3$2Cd9", null);
        assertTrue(valid);
    }

    @Test
    @DisplayName("Password format is invalid because it miss special char")
    void noSpecialChar() {
        boolean valid = passwordPolicyValidator.isValid("clqqyn2cflpi32cd9", constraintContext);
        assertFalse(valid);
    }

    @Test
    @DisplayName("Password format is invalid because it is less than 10 char")
    void toSmall() {
        boolean valid = passwordPolicyValidator.isValid("clqqyn2c*", constraintContext);
        assertFalse(valid);
    }

    @Test
    @DisplayName("Password format is invalid because it is greater than 28 chars")
    void toBig() {
        boolean valid = passwordPolicyValidator.isValid(
                "clqqyn2cflpi32cd9clqqyn2cflpi32cd9clqqyn2cflpi32cd9clqqyn2cflpi32cd9clqqyn2cflpi32cd9clqqyn2cflpi32cd9clqqyn2cflpi32cd9clqqyn2cflpi32cd9",
                constraintContext);
        assertFalse(valid);
    }

    @Test
    @DisplayName("Password format is invalid because it to many repetitive chars")
    void tooRepetitive() {
        boolean valid = passwordPolicyValidator.isValid("&CLQQQQYN2CFLPI*3$2CD9", constraintContext);
        assertFalse(valid);
    }

    @Test
    @DisplayName("Password format is invalid because it is null")
    void nullPassword() {
        boolean valid = passwordPolicyValidator.isValid(null, constraintContext);
        assertFalse(valid);
    }
}

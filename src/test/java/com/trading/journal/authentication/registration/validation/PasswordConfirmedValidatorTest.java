package com.trading.journal.authentication.registration.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.trading.journal.authentication.authentication.ChangePassword;
import com.trading.journal.authentication.registration.UserRegistration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasswordConfirmedValidatorTest {

    private final PasswordConfirmedValidator passwordConfirmedValidator = new PasswordConfirmedValidator();

    @Test
    @DisplayName("Validation of password format for user registration is successfully")
    void passwordsAreValid() {
        UserRegistration userRegistration = new UserRegistration("name", "last", "user", "mail", "123", "123");
        boolean valid = passwordConfirmedValidator.isValid(userRegistration, null);
        assertTrue(valid);
    }

    @Test
    @DisplayName("Validation of password format for user registration is invalid because are not equal")
    void passwordsAreInvalid() {
        UserRegistration userRegistration = new UserRegistration("name", "last", "user", "mail", "123", "1234");
        boolean valid = passwordConfirmedValidator.isValid(userRegistration, null);
        assertFalse(valid);
    }

    @Test
    @DisplayName("Validation of password for user registration is invalid because password is null")
    void invalidNullPassword() {
        UserRegistration userRegistration = new UserRegistration("name", "last", "user", "mail", null, "1234");
        boolean valid = passwordConfirmedValidator.isValid(userRegistration, null);
        assertFalse(valid);
    }

    @Test
    @DisplayName("Validation of password for user registration is invalid because password confirmation is null")
    void invalidNullConfirmation() {
        UserRegistration userRegistration = new UserRegistration("name", "last", "user", "mail", "123", null);
        boolean valid = passwordConfirmedValidator.isValid(userRegistration, null);
        assertFalse(valid);
    }

    @Test
    @DisplayName("Validation of password format for ChangePassword is successfully")
    void passwordsAreValidChangePassword() {
        ChangePassword userRegistration = new ChangePassword("name", "hash", "123", "123");
        boolean valid = passwordConfirmedValidator.isValid(userRegistration, null);
        assertTrue(valid);
    }

    @Test
    @DisplayName("Validation of password format for ChangePassword is invalid because are not equal")
    void passwordsAreInvalidChangePassword() {
        ChangePassword userRegistration = new ChangePassword("name", "hash", "123", "1234");
        boolean valid = passwordConfirmedValidator.isValid(userRegistration, null);
        assertFalse(valid);
    }

    @Test
    @DisplayName("Validation of password for ChangePassword is invalid because password is null")
    void invalidNullPasswordChangePassword() {
        ChangePassword userRegistration = new ChangePassword("name", "hash", null, "123");
        boolean valid = passwordConfirmedValidator.isValid(userRegistration, null);
        assertFalse(valid);
    }

    @Test
    @DisplayName("Validation of password for ChangePassword is invalid because password confirmation is null")
    void invalidNullConfirmationChangePassword() {
        ChangePassword userRegistration = new ChangePassword("name", "hash", "123", null);
        boolean valid = passwordConfirmedValidator.isValid(userRegistration, null);
        assertFalse(valid);
    }
}

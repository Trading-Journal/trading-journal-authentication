package com.trading.journal.authentication.registration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRegistrationTest {

    @DisplayName("When company exists get it")
    @Test
    void getCompany(){
        UserRegistration user = new UserRegistration(
                "Allan CO",
                "allan",
                "weber",
                "allanweber",
                "email@mail.com",
                "12345",
                "12345");

        assertThat(user.getCompanyName()).isEqualTo("Allan CO");
    }

    @DisplayName("When company does not exists get user name as company name")
    @Test
    void getCompanyUserName(){
        UserRegistration user = new UserRegistration(
                null,
                "allan",
                "weber",
                "allanweber",
                "email@mail.com",
                "12345",
                "12345");

        assertThat(user.getCompanyName()).isEqualTo("allanweber");
    }
}
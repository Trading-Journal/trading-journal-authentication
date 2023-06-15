package com.trading.journal.authentication.user.service;

import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.user.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService {

    Optional<User> getUserByEmail(String email);

    User createNewUser(@NotNull UserRegistration userRegistration, Tenancy tenancy);

    Boolean validateNewUser(String email);

    Boolean emailExists(String email);

    void verifyUser(String email);

    void unprovenUser(String email);

    User changePassword(String email, String password);

    Boolean existsByTenancyId(Long tenancyId);
}

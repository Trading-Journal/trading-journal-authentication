package com.trading.journal.authentication.user.service;

import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.UserInfo;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

@Service
public interface ApplicationUserService extends UserDetailsService {

    ApplicationUser getUserByEmail(String email);

    ApplicationUser createNewUser(@NotNull UserRegistration userRegistration);

    Boolean validateNewUser(@NotNull String userName, String email);

    Boolean userNameExists(String userName);

    Boolean emailExists(String email);

    UserInfo getUserInfo(String userName);

    void verifyNewUser(String email);

    ApplicationUser changePassword(String email, String password);
}

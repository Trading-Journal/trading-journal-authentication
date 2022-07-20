package com.trading.journal.authentication.user.service;

import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserInfo;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

@Service
public interface UserService {

    User getUserByEmail(String email);

    User createNewUser(@NotNull UserRegistration userRegistration);

    Boolean validateNewUser(@NotNull String userName, String email);

    Boolean userNameExists(String userName);

    Boolean emailExists(String email);

    UserInfo getUserInfo(String email);

    void verifyUser(String email);

    void unprovenUser(String email);

    User changePassword(String email, String password);
}

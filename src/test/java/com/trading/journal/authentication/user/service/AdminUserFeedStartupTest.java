package com.trading.journal.authentication.user.service;

import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.user.properties.AdminUserProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AdminUserFeedStartupTest {

    @Mock
    AdminUserService adminUserService;

    @DisplayName("Execute creation of a admin user")
    @Test
    void createAdmin() {
        AdminUserProperties properties = new AdminUserProperties("mail@mail.com");
        AdminUserFeedStartup adminUserFeedStartup = new AdminUserFeedStartup(adminUserService, properties);

        when(adminUserService.thereIsAdmin()).thenReturn(false);

        adminUserFeedStartup.onApplicationEvent(null);

        UserRegistration userRegistration = new UserRegistration("Admin", "Administrator", "admin", "mail@mail.com", null, null);
        verify(adminUserService).createAdmin(userRegistration);
    }

    @DisplayName("Do not creation a admin user")
    @Test
    void notCreateAdmin() {
        AdminUserFeedStartup adminUserFeedStartup = new AdminUserFeedStartup(adminUserService, null);
        when(adminUserService.thereIsAdmin()).thenReturn(true);
        adminUserFeedStartup.onApplicationEvent(null);
        verify(adminUserService, never()).createAdmin(any());
    }
}
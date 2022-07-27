package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.registration.SignUpResponse;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.registration.service.RegistrationService;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.service.TenancyService;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.user.UserManagementRepository;
import com.trading.journal.authentication.user.service.UserService;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UserManagementServiceImplTest {

    @Mock
    UserManagementRepository userManagementRepository;

    @Mock
    UserAuthorityService userAuthorityService;

    @Mock
    TenancyService tenancyService;

    @Mock
    UserService userService;

    @Mock
    RegistrationService registrationService;

    @InjectMocks
    UserManagementServiceImpl applicationUserManagementService;

    @DisplayName("Given page request page users query without filter")
    @Test
    void pageWithoutFilter() {
        PageableRequest pageableRequest = new PageableRequest(0, 10, null, null);

        when(userManagementRepository.findAll(any(), eq(pageableRequest.pageable()))).thenReturn(new PageImpl<>(
                singletonList(User.builder()
                        .id(1L)
                        .userName("UserName")
                        .password("password_secret")
                        .firstName("lastName")
                        .lastName("Wick")
                        .email("mail@mail.com")
                        .enabled(true)
                        .verified(true)
                        .createdAt(LocalDateTime.now())
                        .authorities(singletonList(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))))
                        .build()),
                pageableRequest.pageable(),
                2
        ));
        PageResponse<UserInfo> response = applicationUserManagementService.getAll(10L, pageableRequest);
        assertThat(response.items()).hasSize(1);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.totalItems()).isEqualTo(1L);
        assertThat(response.currentPage()).isEqualTo(0);
    }

    @DisplayName("Given page request page users query with filter")
    @Test
    void pageWithFilter() {
        PageableRequest pageableRequest = new PageableRequest(0, 10, null, "any string");

        when(userManagementRepository.findAll(any(), eq(pageableRequest.pageable()))).thenReturn(new PageImpl<>(
                singletonList(User.builder()
                        .id(1L)
                        .userName("UserName")
                        .password("password_secret")
                        .firstName("lastName")
                        .lastName("Wick")
                        .email("mail@mail.com")
                        .enabled(true)
                        .verified(true)
                        .createdAt(LocalDateTime.now())
                        .authorities(singletonList(new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))))
                        .build()),
                pageableRequest.pageable(),
                2
        ));

        PageResponse<UserInfo> response = applicationUserManagementService.getAll(1L, pageableRequest);
        assertThat(response.items()).hasSize(1);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.totalItems()).isEqualTo(1L);
        assertThat(response.currentPage()).isEqualTo(0);
    }

    @DisplayName("Given userId that exists return user")
    @Test
    void getUserById() {
        Long userId = 10L;
        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.of(User.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build()));

        UserInfo userInfo = applicationUserManagementService.getUserById(10L, userId);
        assertThat(userInfo).isNotNull();
    }

    @DisplayName("Given userId that does not exists return not found exception")
    @Test
    void getUserByIdNotFound() {
        Long userId = 10L;
        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> applicationUserManagementService.getUserById(10L, userId));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("User id not found");
    }

    @DisplayName("Disable user successfully")
    @Test
    void disableUser() {
        Long userId = 10L;
        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();

        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.of(applicationUser));

        when(userManagementRepository.save(argThat(user -> user.getEnabled().equals(false)))).thenReturn(applicationUser);

        applicationUserManagementService.disableUserById(10L, userId);
    }

    @DisplayName("Disable user that does not exists return not found exception")
    @Test
    void disableUserNotFound() {
        Long userId = 10L;
        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> applicationUserManagementService.disableUserById(10L, userId));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("User id not found");
    }

    @DisplayName("Enable user successfully")
    @Test
    void enableUser() {
        Long userId = 10L;
        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(false)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();

        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.of(applicationUser));

        when(userManagementRepository.save(argThat(user -> user.getEnabled().equals(true)))).thenReturn(applicationUser);

        applicationUserManagementService.enableUserById(10L, userId);
    }

    @DisplayName("Enable user that does not exists return not found exception")
    @Test
    void enableUserNotFound() {
        Long userId = 10L;
        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> applicationUserManagementService.enableUserById(10L, userId));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("User id not found");
    }

    @DisplayName("delete user successfully")
    @Test
    void deleteUser() {
        Long userId = 10L;
        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(false)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();

        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.of(applicationUser));
        when(tenancyService.lowerUsage(10L)).thenReturn(Tenancy.builder().build());

        applicationUserManagementService.deleteUserById(10L, userId);

        verify(userManagementRepository).delete(applicationUser);
    }

    @DisplayName("Delete user that does not exists return not found exception")
    @Test
    void deleteUserNotFound() {
        Long userId = 10L;
        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> applicationUserManagementService.deleteUserById(10L, userId));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("User id not found");

        verify(tenancyService, never()).lowerUsage(anyLong());
    }

    @DisplayName("Add user authorities successfully")
    @Test
    void changeUserAuthorities() {
        Long userId = 10L;
        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(false)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();

        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.of(applicationUser));

        List<UserAuthority> userAuthorities = Arrays.asList(
                new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")),
                new UserAuthority(null, new Authority(2L, AuthorityCategory.ADMINISTRATOR, "ROLE_ADMIN"))
        );
        AuthoritiesChange change = new AuthoritiesChange(singletonList("ROLE_ADMIN"));
        when(userAuthorityService.addAuthorities(applicationUser, change))
                .thenReturn(userAuthorities);

        List<UserAuthority> actualAuthorities = applicationUserManagementService.addAuthorities(10L, userId, change);
        assertThat(actualAuthorities).hasSize(2);
    }

    @DisplayName("Add user authorities that does not exists return not found exception")
    @Test
    void changeUserAuthoritiesNotFound() {
        Long userId = 10L;
        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> applicationUserManagementService.addAuthorities(10L, userId, new AuthoritiesChange(singletonList("USER_ROLE")))
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("User id not found");
    }

    @DisplayName("Remove user authorities successfully")
    @Test
    void removeUserAuthorities() {
        Long userId = 10L;
        User applicationUser = User.builder()
                .id(1L)
                .userName("UserName")
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(false)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(Arrays.asList(
                        new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER")),
                        new UserAuthority(null, new Authority(2L, AuthorityCategory.ADMINISTRATOR, "ROLE_ADMIN"))
                ))
                .build();

        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.of(applicationUser));

        List<UserAuthority> userAuthorities = singletonList(
                new UserAuthority(null, new Authority(1L, AuthorityCategory.COMMON_USER, "ROLE_USER"))
        );
        AuthoritiesChange change = new AuthoritiesChange(singletonList("ROLE_ADMIN"));
        when(userAuthorityService.deleteAuthorities(applicationUser, change))
                .thenReturn(userAuthorities);

        List<UserAuthority> actualAuthorities = applicationUserManagementService.deleteAuthorities(10L, userId, change);
        assertThat(actualAuthorities).hasSize(1);
    }

    @DisplayName("Remove user authorities that does not exists return not found exception")
    @Test
    void removeUserAuthoritiesNotFound() {
        Long userId = 10L;
        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> applicationUserManagementService.deleteAuthorities(10L, userId, new AuthoritiesChange(singletonList("USER_ROLE")))
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("User id not found");
    }

    @DisplayName("Create a new user when tenancy is not found thrown an exception")
    @Test
    void createUserTenancyException() {
        Long tenancyId = 1L;
        when(tenancyService.getById(tenancyId)).thenThrow(new ApplicationException(""));

        assertThrows(ApplicationException.class,
                () -> applicationUserManagementService.create(tenancyId, UserRegistration.builder().build()));

        verify(userService, never()).createNewUser(any(), any());
        verify(registrationService, never()).sendVerification(anyString());
        verify(tenancyService, never()).increaseUsage(anyLong());
    }

    @DisplayName("Create a new user when tenancy is not allowed to increase usage thrown an exception")
    @Test
    void createUserTenancyIncreaseNotAllowed() {
        Long tenancyId = 1L;
        when(tenancyService.getById(tenancyId)).thenReturn(Tenancy.builder().userUsage(10).userLimit(10).build());

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> applicationUserManagementService.create(tenancyId, UserRegistration.builder().build()));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Tenancy has reach its user limit");

        verify(userService, never()).createNewUser(any(), any());
        verify(registrationService, never()).sendVerification(anyString());
        verify(tenancyService, never()).increaseUsage(anyLong());
    }

    @DisplayName("Create a new user ")
    @Test
    void createUser() {
        Tenancy tenancy = Tenancy.builder().id(1L).userUsage(1).userLimit(10).build();
        when(tenancyService.getById(1L)).thenReturn(tenancy);

        UserRegistration userRegistration = UserRegistration.builder()
                .email("mail@mail.com")
                .build();

        when(userService.createNewUser(userRegistration, tenancy)).thenReturn(User.builder().email("mail@mail.com").build());
        when(registrationService.sendVerification("mail@mail.com")).thenReturn(new SignUpResponse("mail@mail.com", true));
        when(tenancyService.increaseUsage(1L)).thenReturn(tenancy);

        UserInfo userInfo = applicationUserManagementService.create(1L, userRegistration);

        assertThat(userInfo.getEmail()).isEqualTo("mail@mail.com");
    }
}
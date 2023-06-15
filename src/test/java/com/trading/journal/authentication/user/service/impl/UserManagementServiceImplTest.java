package com.trading.journal.authentication.user.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.registration.UserRegistration;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.service.TenancyService;
import com.trading.journal.authentication.user.*;
import com.trading.journal.authentication.user.service.UserService;
import com.trading.journal.authentication.userauthority.UserAuthority;
import com.trading.journal.authentication.userauthority.UserAuthorityResponse;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationType;
import com.trading.journal.authentication.verification.service.VerificationService;
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
    VerificationService verificationService;

    @InjectMocks
    UserManagementServiceImpl userManagementService;

    @DisplayName("Given page request page users query without filter")
    @Test
    void pageWithoutFilter() {
        PageableRequest pageableRequest = new PageableRequest(0, 10, null, null);

        when(userManagementRepository.findAll(any(), eq(pageableRequest.pageable()))).thenReturn(new PageImpl<>(
                singletonList(User.builder()
                        .id(1L)
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
        PageResponse<UserInfo> response = userManagementService.getAll(10L, pageableRequest);
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

        PageResponse<UserInfo> response = userManagementService.getAll(1L, pageableRequest);
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
                .password("password_secret")
                .firstName("lastName")
                .lastName("Wick")
                .email("mail@mail.com")
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build()));

        UserInfo userInfo = userManagementService.getUserById(10L, userId);
        assertThat(userInfo).isNotNull();
    }

    @DisplayName("Given userId that does not exists return not found exception")
    @Test
    void getUserByIdNotFound() {
        Long userId = 10L;
        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> userManagementService.getUserById(10L, userId));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("User not found");
    }

    @DisplayName("Disable user successfully")
    @Test
    void disableUser() {
        Long userId = 10L;
        User applicationUser = User.builder()
                .id(1L)
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

        userManagementService.disableUserById(10L, userId);
    }

    @DisplayName("Disable user that does not exists return not found exception")
    @Test
    void disableUserNotFound() {
        Long userId = 10L;
        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> userManagementService.disableUserById(10L, userId));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("User not found");
    }

    @DisplayName("Enable user successfully")
    @Test
    void enableUser() {
        Long userId = 10L;
        User applicationUser = User.builder()
                .id(1L)
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

        userManagementService.enableUserById(10L, userId);
    }

    @DisplayName("Enable user that does not exists return not found exception")
    @Test
    void enableUserNotFound() {
        Long userId = 10L;
        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> userManagementService.enableUserById(10L, userId));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("User not found");
    }

    @DisplayName("delete user successfully")
    @Test
    void deleteUser() {
        Long userId = 10L;
        User applicationUser = User.builder()
                .id(1L)
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

        userManagementService.deleteUserById(10L, userId);

        verify(userManagementRepository).delete(applicationUser);
    }

    @DisplayName("Delete user that does not exists return not found exception")
    @Test
    void deleteUserNotFound() {
        Long userId = 10L;
        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> userManagementService.deleteUserById(10L, userId));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("User not found");

        verify(tenancyService, never()).lowerUsage(anyLong());
    }

    @DisplayName("Add user authorities successfully")
    @Test
    void changeUserAuthorities() {
        Long userId = 10L;
        User applicationUser = User.builder()
                .id(1L)
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

        List<UserAuthorityResponse> actualAuthorities = userManagementService.addAuthorities(10L, userId, change);
        assertThat(actualAuthorities).hasSize(2);
    }

    @DisplayName("Add user authorities that does not exists return not found exception")
    @Test
    void changeUserAuthoritiesNotFound() {
        Long userId = 10L;
        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> userManagementService.addAuthorities(10L, userId, new AuthoritiesChange(singletonList("USER_ROLE")))
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("User not found");
    }

    @DisplayName("Remove user authorities successfully")
    @Test
    void removeUserAuthorities() {
        Long userId = 10L;
        User applicationUser = User.builder()
                .id(1L)
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

        List<UserAuthorityResponse> actualAuthorities = userManagementService.deleteAuthorities(10L, userId, change);
        assertThat(actualAuthorities).hasSize(1);
    }

    @DisplayName("Remove user authorities that does not exists return not found exception")
    @Test
    void removeUserAuthoritiesNotFound() {
        Long userId = 10L;
        when(userManagementRepository.findByTenancyIdAndId(10L, userId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> userManagementService.deleteAuthorities(10L, userId, new AuthoritiesChange(singletonList("USER_ROLE")))
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("User not found");
    }

    @DisplayName("Create a new user when tenancy is not found thrown an exception")
    @Test
    void createUserTenancyException() {
        Long tenancyId = 1L;
        when(tenancyService.getById(tenancyId)).thenThrow(new ApplicationException(""));

        assertThrows(ApplicationException.class,
                () -> userManagementService.create(tenancyId, UserRegistration.builder().build()));

        verify(userService, never()).createNewUser(any(), any());
        verify(verificationService, never()).send(any(), any());
        verify(tenancyService, never()).increaseUsage(anyLong());
    }

    @DisplayName("Create a new user when tenancy is not allowed to increase usage thrown an exception")
    @Test
    void createUserTenancyIncreaseNotAllowed() {
        Long tenancyId = 1L;
        when(tenancyService.getById(tenancyId)).thenReturn(Tenancy.builder().userUsage(10).userLimit(10).build());

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> userManagementService.create(tenancyId, UserRegistration.builder().build()));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Tenancy has reach its user limit");

        verify(userService, never()).createNewUser(any(), any());
        verify(verificationService, never()).send(any(), any());
        verify(tenancyService, never()).increaseUsage(anyLong());
    }

    @DisplayName("Create a new user ")
    @Test
    void createUser() {
        Tenancy tenancy = Tenancy.builder().id(1L).userUsage(1).userLimit(10).build();
        when(tenancyService.getById(1L)).thenReturn(tenancy);

        UserRegistration userRegistration = UserRegistration.builder().email("mail@mail.com").build();
        User user = User.builder().email("mail@mail.com").build();

        when(userService.createNewUser(argThat(registration -> registration.getEmail().equals("mail@mail.com")), eq(tenancy))).thenReturn(user);
        doNothing().when(verificationService).send(VerificationType.NEW_ORGANISATION_USER, user);
        when(tenancyService.increaseUsage(1L)).thenReturn(tenancy);

        UserInfo userInfo = userManagementService.create(1L, userRegistration);

        assertThat(userInfo.getEmail()).isEqualTo("mail@mail.com");
    }

    @DisplayName("Request to delete me")
    @Test
    void requestToDeleteMe() {
        String email = "mail@mail.com";
        Long tenancyId = 10L;
        User user = User.builder()
                .id(1L)
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email(email)
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();

        when(userManagementRepository.findByTenancyIdAndEmail(tenancyId, email)).thenReturn(Optional.of(user));
        doNothing().when(verificationService).send(VerificationType.DELETE_ME, user);

        userManagementService.deleteMeRequest(tenancyId, email);
    }

    @DisplayName("Request to delete me email not found throw an exception")
    @Test
    void requestToDeleteMeError() {
        String email = "mail@mail.com";
        Long tenancyId = 10L;
        when(userManagementRepository.findByTenancyIdAndEmail(tenancyId, email)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                userManagementService.deleteMeRequest(tenancyId, email));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Email mail@mail.com not found");

        verify(verificationService, never()).send(any(), any());
    }

    @DisplayName("Delete me retrieve hash exception")
    @Test
    void deleteMeRetrieveHash() {
        when(verificationService.retrieve(anyString())).thenThrow(new ApplicationException(HttpStatus.BAD_REQUEST, "Request is invalid"));

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                userManagementService.deleteMe(10L, "mail@mail.com", "hash"));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Request is invalid");

        verify(userManagementRepository, never()).findByTenancyIdAndEmail(any(), any());
        verify(userManagementRepository, never()).delete(any(User.class));
        verify(tenancyService, never()).lowerUsage(anyLong());
        verify(verificationService, never()).verify(any());
        verify(userService, never()).existsByTenancyId(anyLong());
        verify(tenancyService, never()).delete(anyLong());
    }

    @DisplayName("Delete me email and verification email are different")
    @Test
    void deleteMeDifferentEmails() {
        String email = "mail@mail.com";

        Verification verification = Verification.builder()
                .hash("123")
                .email("othermail@mail.com")
                .build();
        when(verificationService.retrieve(anyString())).thenReturn(verification);

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                userManagementService.deleteMe(10L, email, "hash"));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Verification does not exist or is invalid");

        verify(userManagementRepository, never()).findByTenancyIdAndEmail(any(), any());
        verify(userManagementRepository, never()).delete(any(User.class));
        verify(tenancyService, never()).lowerUsage(anyLong());
        verify(verificationService, never()).verify(any());
        verify(userService, never()).existsByTenancyId(anyLong());
        verify(tenancyService, never()).delete(anyLong());
    }

    @DisplayName("Delete me email not found")
    @Test
    void deleteMeEmailNotFound() {
        String email = "mail@mail.com";
        Long tenancyId = 10L;

        Verification verification = Verification.builder()
                .hash("123")
                .email(email)
                .build();
        when(verificationService.retrieve(anyString())).thenReturn(verification);

        when(userManagementRepository.findByTenancyIdAndEmail(tenancyId, email)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                userManagementService.deleteMe(tenancyId, email, "hash"));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Email mail@mail.com not found");

        verify(userManagementRepository, never()).delete(any(User.class));
        verify(tenancyService, never()).lowerUsage(anyLong());
        verify(verificationService, never()).verify(any());
        verify(userService, never()).existsByTenancyId(anyLong());
        verify(tenancyService, never()).delete(anyLong());
    }

    @DisplayName("Delete me but do not delete tenancy because there is still users there")
    @Test
    void deleteMeNoTenancyDelete() {
        String email = "mail@mail.com";
        Long tenancyId = 10L;

        Verification verification = Verification.builder()
                .hash("123")
                .email(email)
                .build();
        when(verificationService.retrieve(anyString())).thenReturn(verification);

        User user = User.builder()
                .id(1L)
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email(email)
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();
        when(userManagementRepository.findByTenancyIdAndEmail(tenancyId, email)).thenReturn(Optional.of(user));
        when(userManagementRepository.findByTenancyIdAndId(tenancyId, 1L)).thenReturn(Optional.of(user));

        doNothing().when(userManagementRepository).delete(user);
        when(tenancyService.lowerUsage(tenancyId)).thenReturn(Tenancy.builder().build());
        doNothing().when(verificationService).verify(verification);
        when(userService.existsByTenancyId(tenancyId)).thenReturn(true);

        userManagementService.deleteMe(tenancyId, email, "hash");

        verify(tenancyService, never()).delete(anyLong());
    }

    @DisplayName("Delete me and delete tenancy")
    @Test
    void deleteMeAndTenancy() {
        String email = "mail@mail.com";
        Long tenancyId = 10L;

        Verification verification = Verification.builder()
                .hash("123")
                .email(email)
                .build();
        when(verificationService.retrieve(anyString())).thenReturn(verification);

        User user = User.builder()
                .id(1L)
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email(email)
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();
        when(userManagementRepository.findByTenancyIdAndEmail(tenancyId, email)).thenReturn(Optional.of(user));
        when(userManagementRepository.findByTenancyIdAndId(tenancyId, 1L)).thenReturn(Optional.of(user));

        doNothing().when(userManagementRepository).delete(user);
        when(tenancyService.lowerUsage(tenancyId)).thenReturn(Tenancy.builder().build());
        doNothing().when(verificationService).verify(verification);
        when(userService.existsByTenancyId(tenancyId)).thenReturn(false);
        doNothing().when(tenancyService).delete(tenancyId);

        userManagementService.deleteMe(tenancyId, email, "hash");
    }

    @DisplayName("Get user by Tenancy and Email")
    @Test
    void getUserByEmail() {
        String email = "mail@mail.com";
        Long tenancyId = 10L;
        User user = User.builder()
                .id(1L)
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email(email)
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();

        when(userManagementRepository.findByTenancyIdAndEmail(tenancyId, email)).thenReturn(Optional.of(user));

        UserInfo userInfo = userManagementService.getUserByEmail(tenancyId, email);
        assertThat(userInfo.getEmail()).isEqualTo(user.getEmail());
        assertThat(userInfo.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userInfo.getLastName()).isEqualTo(user.getLastName());
    }

    @DisplayName("Get user by Tenancy and Email not found")
    @Test
    void getUserByEmailNotFound() {
        String email = "mail@mail.com";
        Long tenancyId = 10L;
        when(userManagementRepository.findByTenancyIdAndEmail(tenancyId, email)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> userManagementService.getUserByEmail(tenancyId, email));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("User not found");
    }

    @DisplayName("Update me same user email exists")
    @Test
    void updateNotFound() {
        String email = "mail@mail.com";
        Long tenancyId = 10L;

        MeUpdate meUpdate = new MeUpdate("firstName-Updated", "lastName-Updated");

        when(userManagementRepository.findByTenancyIdAndEmail(tenancyId, email)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> userManagementService.update(tenancyId, email, meUpdate));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("User not found");

        verify(userManagementRepository, never()).existsByTenancyIdAndEmailAndIdNot(anyLong(), anyString(), anyLong());
        verify(userManagementRepository, never()).save(any());
    }

    @DisplayName("Update me not found")
    @Test
    void updateSameUserName() {
        String email = "mail@mail.com";
        Long tenancyId = 10L;
        MeUpdate meUpdate = new MeUpdate("firstName-Updated", "lastName-Updated");

        User user = User.builder()
                .id(1L)
                .password("password")
                .firstName("lastName")
                .lastName("Wick")
                .email(email)
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();

        when(userManagementRepository.findByTenancyIdAndEmail(tenancyId, email)).thenReturn(Optional.of(user));

        when(userManagementRepository.existsByTenancyIdAndEmailAndIdNot(tenancyId, email, 1L)).thenReturn(true);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> userManagementService.update(tenancyId, email, meUpdate));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getStatusText()).isEqualTo("User name already exists for another user");

        verify(userManagementRepository, never()).save(any());
    }

    @DisplayName("Update me")
    @Test
    void update() {
        String email = "mail@mail.com";
        Long tenancyId = 10L;
        MeUpdate meUpdate = new MeUpdate("firstName-Updated", "lastName-Updated");

        User user = User.builder()
                .id(1L)
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .email(email)
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build();

        when(userManagementRepository.findByTenancyIdAndEmail(tenancyId, email)).thenReturn(Optional.of(user));

        when(userManagementRepository.existsByTenancyIdAndEmailAndIdNot(tenancyId, email, 1L)).thenReturn(false);

        when(userManagementRepository.save(argThat(u ->
                u.getFirstName().equals("firstName-Updated")
                        && u.getLastName().equals("lastName-Updated")
        ))).thenReturn(User.builder()
                .id(1L)
                .password("password")
                .firstName("firstName-Updated")
                .lastName("lastName-Updated")
                .email(email)
                .enabled(true)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .authorities(emptyList())
                .build());

        UserInfo userInfo = userManagementService.update(tenancyId, email, meUpdate);
        assertThat(userInfo.getFirstName()).isEqualTo("firstName-Updated");
        assertThat(userInfo.getLastName()).isEqualTo("lastName-Updated");
    }
}
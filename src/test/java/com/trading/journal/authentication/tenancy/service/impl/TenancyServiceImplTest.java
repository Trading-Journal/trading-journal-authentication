package com.trading.journal.authentication.tenancy.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.TenancyRepository;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class TenancyServiceImplTest {

    @Mock
    TenancyRepository tenancyRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    TenancyServiceImpl tenancyService;

    @DisplayName("Given page request page tenancies query without filter")
    @Test
    void pageWithoutFilter() {
        PageableRequest pageableRequest = new PageableRequest(0, 10, null, null);

        when(tenancyRepository.findAll(null, pageableRequest.pageable())).thenReturn(new PageImpl<>(singletonList(Tenancy.builder().id(1L).name("tenancy1").build()), pageableRequest.pageable(), 2));
        PageResponse<Tenancy> response = tenancyService.getAll(pageableRequest);
        assertThat(response.items()).hasSize(1);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.totalItems()).isEqualTo(1L);
        assertThat(response.currentPage()).isEqualTo(0);
    }

    @DisplayName("Given page request page tenancies query with filter")
    @Test
    void pageWithFilter() {
        PageableRequest pageableRequest = new PageableRequest(0, 10, null, "any filter");

        when(tenancyRepository.findAll(any(), eq(pageableRequest.pageable()))).thenReturn(new PageImpl<>(singletonList(Tenancy.builder().id(1L).name("tenancy1").build()), pageableRequest.pageable(), 2));
        PageResponse<Tenancy> response = tenancyService.getAll(pageableRequest);
        assertThat(response.items()).hasSize(1);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.totalItems()).isEqualTo(1L);
        assertThat(response.currentPage()).isEqualTo(0);
    }

    @DisplayName("Find tenancy by id")
    @Test
    void findById() {
        Tenancy tenancy = Tenancy.builder().id(1L).name("tenancy1").build();
        when(tenancyRepository.findById(1L)).thenReturn(Optional.of(tenancy));

        Tenancy tenancy1 = tenancyService.getById(1L);
        assertThat(tenancy1).isEqualTo(tenancy);
    }

    @DisplayName("Find tenancy by id Not found")
    @Test
    void findByIdNotFund() {
        when(tenancyRepository.findById(1L)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> tenancyService.getById(1L));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Tenancy id not found");
    }

    @DisplayName("Save a new tenancy")
    @Test
    void create() {
        Tenancy tenancyToSave = Tenancy.builder().name("tenancy1").build();
        Tenancy tenancySaved = Tenancy.builder().id(1L).name("tenancy1").build();

        when(tenancyRepository.findByName("tenancy1")).thenReturn(Optional.empty());
        when(tenancyRepository.save(tenancyToSave)).thenReturn(tenancySaved);

        Tenancy tenancy = tenancyService.create(tenancyToSave);
        assertThat(tenancy).isEqualTo(tenancySaved);
    }

    @DisplayName("Save tenancy with a new that already exist return exception")
    @Test
    void createError() {
        Tenancy tenancyToSave = Tenancy.builder().name("tenancy1").build();
        Tenancy tenancySaved = Tenancy.builder().id(1L).name("tenancy1").build();

        when(tenancyRepository.findByName("tenancy1")).thenReturn(Optional.of(tenancySaved));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> tenancyService.create(tenancyToSave));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getStatusText()).isEqualTo("Tenancy name 'tenancy1' already exist");

        verify(tenancyRepository, never()).save(any());
    }

    @DisplayName("Disable tenancy by id")
    @Test
    void disable() {
        Tenancy tenancy = Tenancy.builder().id(1L).name("tenancy1").build();
        when(tenancyRepository.findById(1L)).thenReturn(Optional.of(tenancy));
        when(tenancyRepository.save(argThat(saved -> saved.getEnabled().equals(false)))).thenReturn(tenancy);
        tenancyService.disable(1L);
    }

    @DisplayName("Disable tenancy by id not found")
    @Test
    void disableError() {
        when(tenancyRepository.findById(1L)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> tenancyService.disable(1L));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Tenancy id not found");

        verify(tenancyRepository, never()).save(any());
    }

    @DisplayName("Enable tenancy by id")
    @Test
    void enable() {
        Tenancy tenancy = Tenancy.builder().id(1L).name("tenancy1").enabled(false).build();
        when(tenancyRepository.findById(1L)).thenReturn(Optional.of(tenancy));
        when(tenancyRepository.save(argThat(saved -> saved.getEnabled().equals(true)))).thenReturn(tenancy);
        tenancyService.enable(1L);
    }

    @DisplayName("Enable tenancy by id not found")
    @Test
    void enableError() {
        when(tenancyRepository.findById(1L)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> tenancyService.enable(1L));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Tenancy id not found");

        verify(tenancyRepository, never()).save(any());
    }

    @DisplayName("Set tenancy limit by id")
    @Test
    void setLimit() {
        Tenancy tenancy = Tenancy.builder().id(1L).name("tenancy1").userLimit(1).userUsage(1).build();
        when(tenancyRepository.findById(1L)).thenReturn(Optional.of(tenancy));

        Tenancy tenancyNewLimit = Tenancy.builder().id(1L).name("tenancy1").userLimit(1).userUsage(1).build();
        when(tenancyRepository.save(argThat(saved -> saved.getUserLimit().equals(10)))).thenReturn(tenancyNewLimit);

        tenancyService.newLimit(1L, 10);
    }

    @DisplayName("Set tenancy limit by id not found")
    @Test
    void limitNotFoundError() {
        when(tenancyRepository.findById(1L)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> tenancyService.newLimit(1L, 10));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Tenancy id not found");

        verify(tenancyRepository, never()).save(any());
    }

    @DisplayName("Set tenancy limit by id limit is lower than usage")
    @Test
    void limitLowerError() {
        Tenancy tenancy = Tenancy.builder().id(1L).name("tenancy1").userLimit(15).userUsage(11).build();
        when(tenancyRepository.findById(1L)).thenReturn(Optional.of(tenancy));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> tenancyService.newLimit(1L, 10));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("New tenancy limit is lower than the current usage");

        verify(tenancyRepository, never()).save(any());
    }

    @DisplayName("Lower tenancy usage")
    @Test
    void lowerUsage() {
        Tenancy savedTenancy = Tenancy.builder().id(1L).name("tenancy1").userLimit(10).userUsage(5).build();
        when(tenancyRepository.findById(1L)).thenReturn(Optional.of(savedTenancy));

        Tenancy tenancyToSave = Tenancy.builder().id(1L).name("tenancy1").userLimit(10).userUsage(4).build();
        when(tenancyRepository.save(argThat(tenancy -> tenancy.getUserUsage().equals(4)))).thenReturn(tenancyToSave);

        Tenancy tenancy = tenancyService.lowerUsage(1L);
        assertThat(tenancy).isEqualTo(tenancyToSave);
    }

    @DisplayName("Lower tenancy usage when usage and limit are zero do not change it")
    @Test
    void lowerUsageNotChange() {
        Tenancy savedTenancy = Tenancy.builder().id(1L).name("tenancy1").userLimit(0).userUsage(0).build();
        when(tenancyRepository.findById(1L)).thenReturn(Optional.of(savedTenancy));

        Tenancy tenancyToSave = Tenancy.builder().id(1L).name("tenancy1").userLimit(0).userUsage(0).build();
        when(tenancyRepository.save(argThat(tenancy -> tenancy.getUserUsage().equals(0)))).thenReturn(tenancyToSave);

        Tenancy tenancy = tenancyService.lowerUsage(1L);
        assertThat(tenancy).isEqualTo(tenancyToSave);
    }

    @DisplayName("Lower tenancy usage by id not found")
    @Test
    void lowerUsageNotFoundError() {
        when(tenancyRepository.findById(1L)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> tenancyService.lowerUsage(1L));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Tenancy id not found");

        verify(tenancyRepository, never()).save(any());
    }

    @DisplayName("Increase tenancy usage")
    @Test
    void increaseUsage() {
        Tenancy savedTenancy = Tenancy.builder().id(1L).name("tenancy1").userLimit(10).userUsage(5).build();
        when(tenancyRepository.findById(1L)).thenReturn(Optional.of(savedTenancy));

        Tenancy tenancyToSave = Tenancy.builder().id(1L).name("tenancy1").userLimit(10).userUsage(6).build();
        when(tenancyRepository.save(argThat(tenancy -> tenancy.getUserUsage().equals(6)))).thenReturn(tenancyToSave);

        Tenancy tenancy = tenancyService.increaseUsage(1L);
        assertThat(tenancy).isEqualTo(tenancyToSave);
    }

    @DisplayName("Increase tenancy usage by id not found")
    @Test
    void increaseNotFoundError() {
        when(tenancyRepository.findById(1L)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> tenancyService.increaseUsage(1L));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Tenancy id not found");

        verify(tenancyRepository, never()).save(any());
    }

    @DisplayName("Increase tenancy usage has reach its limit return an exception")
    @Test
    void increaseLimitError() {
        Tenancy savedTenancy = Tenancy.builder().id(1L).name("tenancy1").userLimit(10).userUsage(10).build();
        when(tenancyRepository.findById(1L)).thenReturn(Optional.of(savedTenancy));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> tenancyService.increaseUsage(1L));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusText()).isEqualTo("Tenancy has reach its user limit");

        verify(tenancyRepository, never()).save(any());
    }

    @DisplayName("Increase tenancy allowed")
    @Test
    void increaseAllowed() {
        Tenancy savedTenancy = Tenancy.builder().id(1L).name("tenancy1").userLimit(10).userUsage(5).build();
        when(tenancyRepository.findById(1L)).thenReturn(Optional.of(savedTenancy));

        boolean increaseUsageAllowed = tenancyService.increaseUsageAllowed(1L);
        assertThat(increaseUsageAllowed).isTrue();
    }

    @DisplayName("Increase tenancy not allowed")
    @Test
    void increaseNotAllowed() {
        Tenancy savedTenancy = Tenancy.builder().id(1L).name("tenancy1").userLimit(10).userUsage(10).build();
        when(tenancyRepository.findById(1L)).thenReturn(Optional.of(savedTenancy));

        boolean increaseUsageAllowed = tenancyService.increaseUsageAllowed(1L);
        assertThat(increaseUsageAllowed).isFalse();
    }

    @DisplayName("Increase allowed by id not found")
    @Test
    void increaseAllowedNotFoundError() {
        when(tenancyRepository.findById(1L)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> tenancyService.increaseUsageAllowed(1L));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getStatusText()).isEqualTo("Tenancy id not found");

        verify(tenancyRepository, never()).save(any());
    }

    @DisplayName("Find tenancy by user email return tenancy")
    @Test
    void findByEmail() {
        String email = "mail@mail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(User.builder().tenancy(
                Tenancy.builder().name("tenancy").build()
        ).build()));

        Optional<Tenancy> tenancy = tenancyService.getByEmail(email);
        assertThat(tenancy).isPresent();
        assertThat(tenancy.get().getName()).isEqualTo("tenancy");
    }

    @DisplayName("Find tenancy by user email not found")
    @Test
    void findByEmailEmpty() {
        String email = "mail@mail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<Tenancy> tenancy = tenancyService.getByEmail(email);
        assertThat(tenancy).isNotPresent();
    }

    @DisplayName("Delete tenancy by id")
    @Test
    void deleteTenancy() {
        Long tenancyId = 10L;

        when(userRepository.findByTenancyId(tenancyId)).thenReturn(emptyList());

        tenancyService.delete(tenancyId);

        verify(tenancyRepository).deleteById(tenancyId);
    }

    @DisplayName("Delete tenancy by id if there is users return exception")
    @Test
    void deleteTenancyError() {
        Long tenancyId = 10L;

        when(userRepository.findByTenancyId(tenancyId)).thenReturn(
                singletonList(User.builder().tenancy(Tenancy.builder().name("tenancy").build()).build())
        );

        ApplicationException exception = assertThrows(ApplicationException.class, () -> tenancyService.delete(tenancyId));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getStatusText()).isEqualTo("Delete this tenancy not allowed because there are users using it");

        verify(tenancyRepository, never()).deleteById(anyLong());
    }
}
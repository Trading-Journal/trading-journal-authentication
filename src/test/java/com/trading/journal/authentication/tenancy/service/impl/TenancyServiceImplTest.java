package com.trading.journal.authentication.tenancy.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.TenancyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

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
}
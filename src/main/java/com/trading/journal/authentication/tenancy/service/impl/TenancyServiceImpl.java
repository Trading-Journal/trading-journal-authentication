package com.trading.journal.authentication.tenancy.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.pageable.specifications.FilterLike;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.TenancyRepository;
import com.trading.journal.authentication.tenancy.service.TenancyService;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TenancyServiceImpl implements TenancyService {

    private final TenancyRepository tenancyRepository;

    private final UserService userService;

    @Override
    public PageResponse<Tenancy> getAll(PageableRequest pageRequest) {
        Specification<Tenancy> specification = null;
        if (pageRequest.hasFilter()) {
            specification = new FilterLike<Tenancy>(pageRequest.getFilter()).apply(Columns.NAME);
        }
        Page<Tenancy> tenancies = tenancyRepository.findAll(specification, pageRequest.pageable());
        return new PageResponse<>(tenancies.getTotalElements(), tenancies.getTotalPages(), tenancies.getNumber(), tenancies.toList());
    }

    @Override
    public Tenancy getById(Long id) {
        return tenancyRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Tenancy id not found"));
    }

    @Override
    public Tenancy create(Tenancy tenancy) {
        if (tenancyRepository.findByName(tenancy.getName()).isPresent()) {
            throw new ApplicationException(HttpStatus.CONFLICT, String.format("Tenancy name '%s' already exist", tenancy.getName()));
        }
        return tenancyRepository.save(tenancy);
    }

    @Override
    public void disable(Long id) {
        Tenancy tenancy = getById(id);
        tenancy.disable();
        tenancyRepository.save(tenancy);
    }

    @Override
    public void enable(Long id) {
        Tenancy tenancy = getById(id);
        tenancy.enable();
        tenancyRepository.save(tenancy);
    }

    @Override
    public Tenancy newLimit(Long id, Integer limit) {
        Tenancy tenancy = getById(id);
        if (tenancy.getUserUsage() > limit) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "New tenancy limit is lower than the current usage");
        }
        tenancy.newLimit(limit);
        return tenancyRepository.save(tenancy);
    }

    @Override
    public Tenancy lowerUsage(Long id) {
        Tenancy tenancy = getById(id);
        tenancy.lowerUsage();
        return tenancyRepository.save(tenancy);
    }

    @Override
    public Tenancy increaseUsage(Long id) {
        Tenancy tenancy = getById(id);
        if (tenancy.increaseUsageAllowed()) {
            tenancy.increaseUsage();
            return tenancyRepository.save(tenancy);
        }
        throw new ApplicationException("Tenancy has reach its user limit");
    }

    @Override
    public boolean increaseUsageAllowed(Long id) {
        Tenancy tenancy = getById(id);
        return tenancy.increaseUsageAllowed();
    }

    @Override
    public Optional<Tenancy> getByEmail(String email) {
        return userService.getUserByEmail(email)
                .map(User::getTenancy);
    }

    @Override
    public void delete(Long id) {
        Boolean userInTenancy = userService.existsByTenancyId(id);
        if (userInTenancy) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Delete this tenancy not allowed because there are users using it");
        } else {
            tenancyRepository.deleteById(id);
        }
    }

    private static class Columns {
        public static final String NAME = "name";
    }
}

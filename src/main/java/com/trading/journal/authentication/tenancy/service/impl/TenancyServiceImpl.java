package com.trading.journal.authentication.tenancy.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.pageable.specifications.FilterLike;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.TenancyRepository;
import com.trading.journal.authentication.tenancy.service.TenancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TenancyServiceImpl implements TenancyService {

    private static final String MESSAGE = "Tenancy id not found";

    private static final String MESSAGE_NAME = "Tenancy name '%s' already exist";

    private final TenancyRepository tenancyRepository;

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
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, MESSAGE));
    }

    @Override
    public Tenancy create(Tenancy tenancy) {
        if (tenancyRepository.findByName(tenancy.getName()).isPresent()) {
            throw new ApplicationException(HttpStatus.CONFLICT, String.format(MESSAGE_NAME, tenancy.getName()));
        }
        return tenancyRepository.save(tenancy);
    }

    private static class Columns {
        public static final String NAME = "name";
    }
}

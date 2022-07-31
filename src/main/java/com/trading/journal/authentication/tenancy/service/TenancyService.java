package com.trading.journal.authentication.tenancy.service;

import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.pageable.PageableRequest;
import com.trading.journal.authentication.tenancy.Tenancy;

public interface TenancyService {
    PageResponse<Tenancy> getAll(PageableRequest pageRequest);

    Tenancy getById(Long id);

    Tenancy create(Tenancy tenancy);

    void disable(Long id);

    void enable(Long id);

    Tenancy newLimit(Long id, Integer limit);

    Tenancy lowerUsage(Long id);

    Tenancy increaseUsage(Long id);

    boolean increaseUsageAllowed(Long id);
}

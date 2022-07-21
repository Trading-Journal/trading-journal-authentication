package com.trading.journal.authentication.tenancy;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface TenancyRepository extends PagingAndSortingRepository<Tenancy, Long>, JpaSpecificationExecutor<Tenancy> {

    Optional<Tenancy> findByName(String name);
}

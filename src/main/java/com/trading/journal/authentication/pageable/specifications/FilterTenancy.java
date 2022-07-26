package com.trading.journal.authentication.pageable.specifications;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
public class FilterTenancy<T> {

    public static final String TENANCY_COLUMN = "tenancy";

    public static final String TENANCY_ID_COLUMN = "id";

    private final Long value;

    public Specification<T> apply() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(TENANCY_COLUMN).get(TENANCY_ID_COLUMN), value);
    }
}
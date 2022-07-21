package com.trading.journal.authentication.pageable.specifications;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

@RequiredArgsConstructor
public class FilterLike<T> {

    private final String value;

    public Specification<T> apply(String column) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.like(root.get(column), "%" + value.toLowerCase(Locale.getDefault()) + "%");
    }
}

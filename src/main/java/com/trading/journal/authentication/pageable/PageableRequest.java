package com.trading.journal.authentication.pageable;

import com.trading.journal.authentication.ApplicationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageableRequest {

    private static final String COMMA = ",";
    private int page;

    private int size;

    private String[] sort;

    @Getter
    private String filter;

    public Pageable pageable() {
        Sort sortable = loadSort();
        return PageRequest.of(page, size, sortable);
    }

    public boolean hasFilter() {
        return StringUtils.hasText(filter);
    }

    private Sort loadSort() {
        Sort sortable = Sort.unsorted();
        if (sort != null && sort.length > 0) {
            List<Sort.Order> orders = Arrays.stream(sort).map(
                    sortString -> {
                        String[] split = sortString.split(COMMA);
                        if (split.length % 2 != 0) {
                            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Sort is invalid");
                        }
                        Sort.Direction direction = Sort.Direction.fromString(split[1].trim());
                        if (Sort.Direction.ASC.equals(direction)) {
                            return Sort.Order.asc(split[0].trim());
                        } else {
                            return Sort.Order.desc(split[0].trim());
                        }
                    }
            ).collect(Collectors.toList());
            sortable = Sort.by(orders);
        }
        return sortable;
    }
}

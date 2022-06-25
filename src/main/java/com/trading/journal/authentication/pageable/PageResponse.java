package com.trading.journal.authentication.pageable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"totalItems", "totalPages", "currentPage", "items"})
public record PageResponse<T>(long totalItems, int totalPages, int currentPage, List<T> items) {
}

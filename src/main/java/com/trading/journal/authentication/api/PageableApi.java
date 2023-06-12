package com.trading.journal.authentication.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.authentication.pageable.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface PageableApi<T> {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<PageResponse<T>> getAll(
            AccessTokenInfo accessTokenInfo,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String[] sort,
            @RequestParam(value = "filter", required = false) String filter);

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<T> getById(AccessTokenInfo accessTokenInfo, @PathVariable Long id);
}

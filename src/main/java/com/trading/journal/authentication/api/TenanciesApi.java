package com.trading.journal.authentication.api;

import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.tenancy.Tenancy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/admin/tenancies")
public interface TenanciesApi {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<PageResponse<Tenancy>> getAll(
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String[] sort,
            @RequestParam(value = "filter", required = false) String filter);

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Tenancy> getById(@PathVariable Long id);

    @PatchMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> disable(@PathVariable Long id);

    @PatchMapping("/{id}/enable")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> enable(@PathVariable Long id);

    @PatchMapping("/{id}/limit/{limit}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Tenancy> limit(@PathVariable Long id, @PathVariable Integer limit);

    @GetMapping("/by-email/{email}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Tenancy> getByEmail(@PathVariable String email);

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(@PathVariable Long id);
}

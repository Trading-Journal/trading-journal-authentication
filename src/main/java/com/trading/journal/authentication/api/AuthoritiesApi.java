package com.trading.journal.authentication.api;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/admin/authorities")
public interface AuthoritiesApi {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<Authority>> getAll();

    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<AuthorityCategory>> getAllCategories();

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Authority> getById(@PathVariable Long id);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<Authority> add(@RequestBody Authority authority);

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Authority> update(@PathVariable Long id, @RequestBody Authority authority);

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(@PathVariable Long id);
}

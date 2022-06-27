package com.trading.journal.authentication.api;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.service.AuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class AuthoritiesController implements AuthoritiesApi {

    private final AuthorityService authorityService;

    @Override
    public ResponseEntity<List<Authority>> getAll() {
        return ok(authorityService.getAll());
    }

    @Override
    public ResponseEntity<List<AuthorityCategory>> getAllCategories() {
        return ok(Arrays.stream(AuthorityCategory.values()).toList());
    }

    @Override
    public ResponseEntity<Authority> getById(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<Authority> add(Authority authority) {
        return ok(new Authority(AuthorityCategory.COMMON_USER, "ABC"));
    }

    @Override
    public ResponseEntity<Authority> update(Long id, Authority authority) {
        return null;
    }

    @Override
    public ResponseEntity<Authority> delete(Long id) {
        return null;
    }
}

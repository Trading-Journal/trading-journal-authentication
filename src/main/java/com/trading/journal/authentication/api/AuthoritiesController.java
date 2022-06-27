package com.trading.journal.authentication.api;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.service.AuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.http.ResponseEntity.created;
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
        return ok(authorityService.getAllCategories());
    }

    @Override
    public ResponseEntity<Authority> getById(Long id) {
        return ok(authorityService.getById(id));
    }

    @Override
    public ResponseEntity<Authority> add(Authority authority) {
        Authority authoritySaved = authorityService.add(authority);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(authoritySaved.getId()).toUri();
        return created(uri).body(authoritySaved);
    }

    @Override
    public ResponseEntity<Authority> update(Long id, Authority authority) {
        return ok(authorityService.update(id, authority));
    }

    @Override
    public ResponseEntity<Void> delete(Long id) {
        authorityService.delete(id);
        return ok().build();
    }
}

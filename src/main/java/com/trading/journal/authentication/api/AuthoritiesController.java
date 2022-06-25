package com.trading.journal.authentication.api;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.verification.service.impl.service.AuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
}

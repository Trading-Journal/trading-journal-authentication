package com.trading.journal.authentication.authority.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import com.trading.journal.authentication.authority.AuthorityRepository;
import com.trading.journal.authentication.authority.service.AuthorityService;
import com.trading.journal.authentication.userauthority.UserAuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorityServiceImpl implements AuthorityService {

    private final AuthorityRepository authorityRepository;

    private final UserAuthorityRepository userAuthorityRepository;

    @Override
    public List<Authority> getAuthoritiesByCategory(AuthorityCategory category) {
        return authorityRepository.getByCategory(category);
    }

    @Override
    public List<Authority> getAll() {
        return authorityRepository.findAll();
    }

    @Override
    public Optional<Authority> getByName(String name) {
        return authorityRepository.getByName(name);
    }

    @Override
    public List<AuthorityCategory> getAllCategories() {
        return Arrays.stream(AuthorityCategory.values()).toList();
    }

    @Override
    public Authority getById(Long id) {
        return authorityRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Authority id not found"));
    }

    @Override
    public Authority add(Authority authority) {
        if (this.getByName(authority.getName()).isPresent()) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Authority name already exists");
        }
        return authorityRepository.save(authority);
    }

    @Override
    public Authority update(Long id, Authority authority) {
        Authority savedAuthority = authorityRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Authority id not found"));

        authorityRepository.getByNameAndIdNot(authority.getName(), savedAuthority.getId())
                .ifPresent(a -> {
                    throw new ApplicationException(HttpStatus.CONFLICT, "Authority name already exists");
                });

        return authorityRepository.save(new Authority(id, authority.getCategory(), authority.getName()));
    }

    @Override
    public void delete(Long id) {
        if (authorityRepository.findById(id).isEmpty()) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Authority id not found");
        }
        if (userAuthorityRepository.existsByAuthorityId(id)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Authority is used by one or more user");
        }
        authorityRepository.deleteById(id);
    }
}

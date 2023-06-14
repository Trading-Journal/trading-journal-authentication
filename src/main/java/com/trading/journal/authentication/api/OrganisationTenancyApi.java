package com.trading.journal.authentication.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.tenancy.Tenancy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@RequestMapping("/organisation/tenancy")
public interface OrganisationTenancyApi {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Tenancy> getById(AccessTokenInfo accessTokenInfo);

    @GetMapping("/authorities")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<Authority>> getAuthorities(AccessTokenInfo accessTokenInfo);
}

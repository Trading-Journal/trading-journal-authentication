package com.trading.journal.authentication.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.service.OrganisationAuthorityService;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.service.TenancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@RestController
public class OrganisationTenancyController implements OrganisationTenancyApi {

    private final TenancyService tenancyService;

    private final OrganisationAuthorityService organisationAuthorityService;

    @Override
    public ResponseEntity<Tenancy> getById(AccessTokenInfo accessTokenInfo) {
        return ok(tenancyService.getById(accessTokenInfo.tenancyId()));
    }

    @Override
    public ResponseEntity<List<Authority>> getAuthorities(AccessTokenInfo accessTokenInfo) {
        return ok(organisationAuthorityService.getAllNonAdmin());
    }
}

package com.trading.journal.authentication.api;

import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.tenancy.service.TenancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@RestController
public class OrganisationTenancyController implements OrganisationTenancyApi {

    private final TenancyService tenancyService;

    @Override
    public ResponseEntity<Tenancy> getById(AccessTokenInfo accessTokenInfo) {
        return ok(tenancyService.getById(accessTokenInfo.tenancyId()));
    }
}

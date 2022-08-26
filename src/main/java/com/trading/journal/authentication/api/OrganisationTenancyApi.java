package com.trading.journal.authentication.api;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.tenancy.Tenancy;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Api(tags = "Organisation Users Api")
@RequestMapping("/organisation/tenancy")
public interface OrganisationTenancyApi {

    @ApiOperation(notes = "Get by id", value = "Get record by its id")
    @ApiResponses(@ApiResponse(code = 200, message = "Record retrieved"))
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Tenancy> getById(AccessTokenInfo accessTokenInfo);

    @ApiOperation(notes = "Get Authorities", value = "Get Authorities")
    @ApiResponses(@ApiResponse(code = 200, message = "Records retrieved"))
    @GetMapping("/authorities")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<Authority>> getAuthorities(AccessTokenInfo accessTokenInfo);
}

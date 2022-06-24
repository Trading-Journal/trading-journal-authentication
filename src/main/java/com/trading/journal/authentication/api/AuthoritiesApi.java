package com.trading.journal.authentication.api;

import com.trading.journal.authentication.authority.Authority;
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

@Api(tags = "Users Api")
@RequestMapping("/admin/authorities")
public interface AuthoritiesApi {

    @ApiOperation(notes = "Get all authorities", value = "Get all authorities")
    @ApiResponses(@ApiResponse(code = 200, message = "Authorities retrieved"))
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<Authority>> getAll();
}

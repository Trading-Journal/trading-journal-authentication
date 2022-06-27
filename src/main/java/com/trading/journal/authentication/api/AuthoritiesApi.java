package com.trading.journal.authentication.api;

import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityCategory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "Authorities Api")
@RequestMapping("/admin/authorities")
public interface AuthoritiesApi {

    @ApiOperation(notes = "Get all authorities", value = "Get all authorities")
    @ApiResponses(@ApiResponse(code = 200, message = "Authorities retrieved"))
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<Authority>> getAll();

    @ApiOperation(notes = "Get all authority categories", value = "Get all authority categories")
    @ApiResponses(@ApiResponse(code = 200, message = "Authorities categories retrieved"))
    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<AuthorityCategory>> getAllCategories();

    @ApiOperation(notes = "Get an authority", value = "Get an authority")
    @ApiResponses(@ApiResponse(code = 200, message = "Authority retrieved"))
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Authority> getById(@PathVariable Long id);

    @ApiOperation(notes = "Add a new authority", value = "Add a new authority")
    @ApiResponses(@ApiResponse(code = 200, message = "Authority added"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<Authority> add(@RequestBody @Valid Authority authority);

    @ApiOperation(notes = "Edit an authority", value = "Edit an authority")
    @ApiResponses(@ApiResponse(code = 200, message = "Authority changed"))
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Authority> update(@PathVariable Long id, @RequestBody @Valid Authority authority);

    @ApiOperation(notes = "Delete an authority", value = "Delete an authority")
    @ApiResponses(@ApiResponse(code = 200, message = "Authority deleted"))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(@PathVariable Long id);
}

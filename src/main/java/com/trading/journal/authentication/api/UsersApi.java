package com.trading.journal.authentication.api;

import com.trading.journal.authentication.pageable.PageResponse;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.userauthority.UserAuthority;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Users Api")
@RequestMapping("/admin/users")
public interface UsersApi {

    String TENANCY = "tenancy";

    @ApiOperation(notes = "Get all", value = "Get all")
    @ApiResponses(@ApiResponse(code = 200, message = "Records retrieved"))
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<PageResponse<UserInfo>> getAll(
            @RequestHeader(name = TENANCY) Long tenancy,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size,
            @ApiParam(name = "sort", value = "A array with property and direction such as \"id,asc\", \"name,desc\"") @RequestParam(value = "sort", required = false) String[] sort,
            @RequestParam(value = "filter", required = false) String filter);

    @ApiOperation(notes = "Get by id", value = "Get record by its id")
    @ApiResponses(@ApiResponse(code = 200, message = "Record retrieved"))
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<UserInfo> getById(@RequestHeader(name = TENANCY) Long tenancy, @PathVariable Long id);

    @ApiOperation(notes = "Disable user", value = "Disable user by id")
    @ApiResponses(@ApiResponse(code = 200, message = "User disabled"))
    @PatchMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> disable(@RequestHeader(name = TENANCY) Long tenancy, @PathVariable Long id);

    @ApiOperation(notes = "Enable user", value = "Enable user by id")
    @ApiResponses(@ApiResponse(code = 200, message = "User enabled"))
    @PatchMapping("/{id}/enable")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> enable(@RequestHeader(name = TENANCY) Long tenancy, @PathVariable Long id);

    @ApiOperation(notes = "Delete user", value = "Delete user by id")
    @ApiResponses(@ApiResponse(code = 200, message = "User deleted"))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(@RequestHeader(name = TENANCY) Long tenancy, @PathVariable Long id);

    @ApiOperation(notes = "Add user authorities", value = "Add user authorities by user id")
    @ApiResponses(@ApiResponse(code = 200, message = "User authorities changed"))
    @PutMapping("/{id}/authorities")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<UserAuthority>> addAuthorities(@RequestHeader(name = TENANCY) Long tenancy, @PathVariable Long id, @RequestBody AuthoritiesChange authorities);

    @ApiOperation(notes = "Add user authorities", value = "Add user authorities by user id")
    @ApiResponses(@ApiResponse(code = 200, message = "User authorities changed"))
    @DeleteMapping("/{id}/authorities")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<UserAuthority>> deleteAuthorities(@RequestHeader(name = TENANCY) Long tenancy, @PathVariable Long id, @RequestBody AuthoritiesChange authorities);
}

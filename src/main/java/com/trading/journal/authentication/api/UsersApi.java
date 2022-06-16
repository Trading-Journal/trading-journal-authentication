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
@RequestMapping("/users")
public interface UsersApi {

    @ApiOperation(notes = "Get all users", value = "Get all users")
    @ApiResponses(@ApiResponse(code = 200, message = "Users retrieved"))
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<PageResponse<UserInfo>> getAll(
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size,
            @ApiParam(name = "sort", value = "A array with property and direction such as \"id,asc\", \"name,desc\"") @RequestParam(value = "sort", defaultValue = "id,asc", required = false) String[] sort,
            @RequestParam(value = "filter", required = false) String filter);

    @ApiOperation(notes = "Get a single user", value = "Get a single user by id")
    @ApiResponses(@ApiResponse(code = 200, message = "User retrieved"))
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<UserInfo> getById(@PathVariable Long id);

    @ApiOperation(notes = "Disable user", value = "Disable user by id")
    @ApiResponses(@ApiResponse(code = 200, message = "User disabled"))
    @PostMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> disable(@PathVariable Long id);

    @ApiOperation(notes = "Enable user", value = "Enable user by id")
    @ApiResponses(@ApiResponse(code = 200, message = "User enabled"))
    @PostMapping("/{id}/enable")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> enable(@PathVariable Long id);

    @ApiOperation(notes = "Delete user", value = "Delete user by id")
    @ApiResponses(@ApiResponse(code = 200, message = "User deleted"))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(@PathVariable Long id);

    @ApiOperation(notes = "Add user authorities", value = "Add user authorities by user id")
    @ApiResponses(@ApiResponse(code = 200, message = "User authorities changed"))
    @PutMapping("/{id}/authorities")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<UserAuthority>> addAuthorities(@PathVariable Long id, @RequestBody AuthoritiesChange authorities);

    @ApiOperation(notes = "Add user authorities", value = "Add user authorities by user id")
    @ApiResponses(@ApiResponse(code = 200, message = "User authorities changed"))
    @DeleteMapping("/{id}/authorities")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<UserAuthority>> deleteAuthorities(@PathVariable Long id, @RequestBody AuthoritiesChange authorities);
}

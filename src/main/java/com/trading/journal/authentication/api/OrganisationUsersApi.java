package com.trading.journal.authentication.api;

import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.user.AuthoritiesChange;
import com.trading.journal.authentication.user.UserInfo;
import com.trading.journal.authentication.userauthority.UserAuthority;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Organisation Users Api")
@RequestMapping("/organisation/users")
public interface OrganisationUsersApi extends PageableApi<UserInfo> {

    @ApiOperation(notes = "Disable user", value = "Disable user by id")
    @ApiResponses(@ApiResponse(code = 200, message = "User disabled"))
    @PatchMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> disable(AccessTokenInfo accessTokenInfo, @PathVariable Long id);

    @ApiOperation(notes = "Enable user", value = "Enable user by id")
    @ApiResponses(@ApiResponse(code = 200, message = "User enabled"))
    @PatchMapping("/{id}/enable")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> enable(AccessTokenInfo accessTokenInfo, @PathVariable Long id);

    @ApiOperation(notes = "Delete user", value = "Delete user by id")
    @ApiResponses(@ApiResponse(code = 200, message = "User deleted"))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(AccessTokenInfo accessTokenInfo, @PathVariable Long id);

    @ApiOperation(notes = "Add user authorities", value = "Add user authorities by user id")
    @ApiResponses(@ApiResponse(code = 200, message = "User authorities changed"))
    @PutMapping("/{id}/authorities")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<UserAuthority>> addAuthorities(AccessTokenInfo accessTokenInfo, @PathVariable Long id, @RequestBody AuthoritiesChange authorities);

    @ApiOperation(notes = "Add user authorities", value = "Add user authorities by user id")
    @ApiResponses(@ApiResponse(code = 200, message = "User authorities changed"))
    @DeleteMapping("/{id}/authorities")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<UserAuthority>> deleteAuthorities(AccessTokenInfo accessTokenInfo, @PathVariable Long id, @RequestBody AuthoritiesChange authorities);
}

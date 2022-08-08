package com.trading.journal.authentication.api;

import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.user.UserInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Me Api")
@RequestMapping("/me")
public interface MeApi {

    @ApiOperation(notes = "My user information", value = "Logged user information")
    @ApiResponses(@ApiResponse(code = 200, message = "My information"))
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<UserInfo> me(AccessTokenInfo accessTokenInfo);

    @ApiOperation(notes = "Send a request to delete user", value = "Send a request to delete user")
    @ApiResponses(@ApiResponse(code = 200, message = "Delete request sent"))
    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Void> delete(AccessTokenInfo accessTokenInfo);

    @ApiOperation(notes = "Verification for delete user", value = "Delete verification")
    @ApiResponses({
            @ApiResponse(code = 200, message = "User deleted"),
            @ApiResponse(code = 400, message = "Verification does not exist or is invalid")})
    @DeleteMapping("/delete")
    ResponseEntity<Void> verify(AccessTokenInfo accessTokenInfo, @RequestParam("hash") String hash);
}

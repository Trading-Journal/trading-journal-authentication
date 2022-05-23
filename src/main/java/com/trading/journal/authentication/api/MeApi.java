package com.trading.journal.authentication.api;

import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.user.UserInfo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import reactor.core.publisher.Mono;

@Api(tags = "Me Api")
@RequestMapping("/me")
public interface MeApi {

    @ApiOperation(notes = "My user information", value = "Logged user information")
    @ApiResponses(@ApiResponse(code = 200, message = "My information"))
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    Mono<ResponseEntity<UserInfo>> me(AccessTokenInfo accessTokenInfo);
}

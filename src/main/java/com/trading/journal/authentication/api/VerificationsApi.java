package com.trading.journal.authentication.api;

import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Verifications Api")
@RequestMapping("/admin/verifications")
public interface VerificationsApi {

    @ApiOperation(notes = "Get verifications", value = "Get all verifications for email")
    @ApiResponses(@ApiResponse(code = 200, message = "Verifications retrieved"))
    @GetMapping("/{email}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<Verification>> get(@PathVariable String email);

    @ApiOperation(notes = "Create a verification", value = "Create a verification type for email")
    @ApiResponses(@ApiResponse(code = 200, message = "Verifications created"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<Verification> create(@RequestBody VerificationRequest request);
}

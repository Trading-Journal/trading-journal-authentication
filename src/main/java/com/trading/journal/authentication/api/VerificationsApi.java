package com.trading.journal.authentication.api;

import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/admin/verifications")
public interface VerificationsApi {

    @GetMapping("/{email}")
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<List<Verification>> get(@PathVariable String email);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<Verification> create(@RequestBody VerificationRequest request);
}

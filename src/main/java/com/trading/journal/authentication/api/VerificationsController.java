package com.trading.journal.authentication.api;

import com.trading.journal.authentication.verification.Verification;
import com.trading.journal.authentication.verification.VerificationRequest;
import com.trading.journal.authentication.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@RestController
public class VerificationsController implements VerificationsApi {

    private final VerificationService verificationService;

    @Override
    public ResponseEntity<List<Verification>> get(String email) {
        return ok(verificationService.getByEmail(email));
    }

    @Override
    public ResponseEntity<Verification> create(@Valid VerificationRequest request) {
        Verification verification = verificationService.create(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(verification.getId()).toUri();
        return created(uri).body(verification);
    }
}

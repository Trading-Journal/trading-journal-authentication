package com.trading.journal.authentication.verification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity(name = "Verifications")
public class Verification {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String email;

    @NotNull
    private VerificationType type;

    @NotNull
    private VerificationStatus status;

    @NotBlank
    private String hash;

    private LocalDateTime lastChange;

    public Verification renew(String hash) {
        this.status = VerificationStatus.PENDING;
        this.lastChange = LocalDateTime.now();
        this.hash = hash;
        return this;
    }
}

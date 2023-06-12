package com.trading.journal.authentication.verification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trading.journal.authentication.helper.DateHelper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Enumerated(EnumType.STRING)
    private VerificationType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    private VerificationStatus status;

    @NotBlank
    private String hash;

    @JsonFormat(pattern = DateHelper.DATE_TIME_FORMAT)
    private LocalDateTime lastChange;

    public Verification renew(String hash) {
        this.status = VerificationStatus.PENDING;
        this.lastChange = LocalDateTime.now();
        this.hash = hash;
        return this;
    }
}

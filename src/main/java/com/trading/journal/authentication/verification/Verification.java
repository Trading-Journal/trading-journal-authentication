package com.trading.journal.authentication.verification;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@Table("Verifications")
public class Verification {

    @Id
    private Long id;

    @NotBlank
    private String email;

    @NotNull
    private VerificationType type;

    @NotNull
    private VerificationStatus status;

    @NotBlank
    private String hash;

    @CreatedDate
    private LocalDateTime lastChange;
}

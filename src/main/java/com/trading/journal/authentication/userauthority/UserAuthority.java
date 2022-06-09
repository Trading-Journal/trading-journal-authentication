package com.trading.journal.authentication.userauthority;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@Table("UserAuthorities")
public class UserAuthority {

    @Id
    private Long id;

    private Long userId;

    @NotNull
    private Long authorityId;

    @NotBlank
    private String name;

    public UserAuthority(Long userId, String name, Long authorityId) {
        this.userId = userId;
        this.name = name;
        this.authorityId = authorityId;
    }
}

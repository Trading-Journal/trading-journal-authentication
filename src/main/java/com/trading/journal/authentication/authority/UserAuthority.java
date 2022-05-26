package com.trading.journal.authentication.authority;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

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

    private Long authorityId;

    private String name;

    public UserAuthority(Long userId, String name, Long authorityId) {
        this.userId = userId;
        this.name = name;
        this.authorityId = authorityId;
    }
}

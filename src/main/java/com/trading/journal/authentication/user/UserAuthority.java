package com.trading.journal.authentication.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table("UserAuthorities")
public class UserAuthority {

    @Id
    private Long id;

    private Long userId;

    private String name;

    public UserAuthority(Long userId, String name) {
        this.userId = userId;
        this.name = name;
    }
}

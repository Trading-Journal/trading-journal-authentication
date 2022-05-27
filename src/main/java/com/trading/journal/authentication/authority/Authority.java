package com.trading.journal.authentication.authority;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@Table("Authorities")
public class Authority {

    @Id
    private Long id;

    private AuthorityCategory category;

    private String name;

    public Authority(AuthorityCategory category, String name) {
        this.category = category;
        this.name = name;
    }
}

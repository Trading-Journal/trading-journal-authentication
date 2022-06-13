package com.trading.journal.authentication.userauthority;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity(name = "UserAuthorities")
public class UserAuthority {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long authorityId;

    @NotBlank(message = "Name is required")
    private String name;

    public UserAuthority(Long userId, String name, Long authorityId) {
        this.userId = userId;
        this.name = name;
        this.authorityId = authorityId;
    }
}

package com.trading.journal.authentication.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.jwt.helper.DateHelper;
import com.trading.journal.authentication.userauthority.UserAuthority;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@ToString
public class UserInfo {

    private Long id;

    private String userName;

    private String firstName;

    private String lastName;

    private String email;

    private Boolean enabled;

    private Boolean verified;

    private List<String> authorities;

    @JsonFormat(pattern = DateHelper.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;

    public UserInfo(User applicationUser) {
        this.id = applicationUser.getId();
        this.userName = applicationUser.getUserName();
        this.firstName = applicationUser.getFirstName();
        this.lastName = applicationUser.getLastName();
        this.email = applicationUser.getEmail();
        this.enabled = applicationUser.getEnabled();
        this.verified = applicationUser.getVerified();
        this.createdAt = applicationUser.getCreatedAt();
        this.authorities = ofNullable(applicationUser.getAuthorities())
                .orElse(emptyList())
                .stream()
                .map(UserAuthority::getAuthority)
                .map(Authority::getName)
                .collect(Collectors.toList());
    }
}
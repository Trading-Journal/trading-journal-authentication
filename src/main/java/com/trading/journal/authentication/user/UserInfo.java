package com.trading.journal.authentication.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.jwt.helper.DateHelper;
import com.trading.journal.authentication.userauthority.UserAuthority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class UserInfo {

    @JsonIgnore
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
        this.authorities = applicationUser.getAuthorities().stream().map(UserAuthority::getAuthority).map(Authority::getName).collect(Collectors.toList());
        this.createdAt = applicationUser.getCreatedAt();
    }
}
package com.trading.journal.authentication.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trading.journal.authentication.jwt.helper.DateHelper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    public void loadAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }
}
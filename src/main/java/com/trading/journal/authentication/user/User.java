package com.trading.journal.authentication.user;

import com.allanweber.jwttoken.contract.JwtUserData;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.userauthority.UserAuthority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(name = "Users")
public class User implements JwtUserData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "tenancyId")
    private Tenancy tenancy;

    private String userName;

    private String password;

    private String firstName;

    private String lastName;

    private String email;

    private Boolean enabled;

    private Boolean verified;

    @OneToMany(mappedBy = "user")
    private List<UserAuthority> authorities;

    private LocalDateTime createdAt;

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public void verify() {
        this.verified = true;
    }

    public void unproven() {
        this.verified = false;
    }

    public void changePassword(@NotBlank String newPassword) {
        this.password = newPassword;
    }

    public void setAuthorities(List<UserAuthority> authorities) {
        this.authorities = authorities;
    }

    public void update(String userName, String firstName, String lastName) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public List<String> getUserAuthoritiesName() {
        return ofNullable(getAuthorities()).orElse(emptyList())
                .stream().map(UserAuthority::getAuthority)
                .map(Authority::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String getUserEmail() {
        return getEmail();
    }

    @Override
    public Long getUserTenancyId() {
        return ofNullable(getTenancy()).map(Tenancy::getId).orElse(null);
    }

    @Override
    public String getUserTenancyName() {
        return ofNullable(getTenancy()).map(Tenancy::getName).orElse(null);
    }
}
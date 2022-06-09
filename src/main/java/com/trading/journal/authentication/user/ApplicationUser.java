package com.trading.journal.authentication.user;

import com.trading.journal.authentication.userauthority.UserAuthority;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table("Users")
@EqualsAndHashCode
public class ApplicationUser {

    @Id
    private Long id;

    private String userName;

    private String password;

    private String firstName;

    private String lastName;

    private String email;

    private Boolean enabled;

    private Boolean verified;

    @Transient
    private List<UserAuthority> authorities;

    private LocalDateTime createdAt;

    public void loadAuthorities(List<UserAuthority> authorities) {
        this.authorities = authorities;
    }

    public void enable() {
        this.enabled = true;
    }

    public void verify() {
        this.verified = true;
    }

    public void changePassword(@NotBlank String newPassword) {
        this.password = newPassword;
    }
}
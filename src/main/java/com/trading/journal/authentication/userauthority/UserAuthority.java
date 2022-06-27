package com.trading.journal.authentication.userauthority;

import com.trading.journal.authentication.user.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(name = "UserAuthorities")
public class UserAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId")
    private ApplicationUser applicationUser;

    private Long authorityId;

    @NotBlank(message = "Name is required")
    private String name;

    public void setApplicationUser(ApplicationUser applicationUser) {
        this.applicationUser = applicationUser;
    }

    public UserAuthority(ApplicationUser applicationUser, String name, Long authorityId) {
        this.applicationUser = applicationUser;
        this.name = name;
        this.authorityId = authorityId;
    }
}

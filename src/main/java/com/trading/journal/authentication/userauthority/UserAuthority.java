package com.trading.journal.authentication.userauthority;

import com.trading.journal.authentication.authority.Authority;
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

    @OneToOne
    @JoinColumn(name = "authorityId")
    private Authority authority;

    @NotBlank(message = "Name is required")
    private String name;

    public void setApplicationUser(ApplicationUser applicationUser) {
        this.applicationUser = applicationUser;
    }

    public UserAuthority(ApplicationUser applicationUser, String name, Authority authority) {
        this.applicationUser = applicationUser;
        this.name = name;
        this.authority = authority;
    }
}

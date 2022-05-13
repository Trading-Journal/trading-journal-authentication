package com.trading.journal.authentication.jwt;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class ContextUser extends User {

    private static final long serialVersionUID = 2657274830115424036L;
    private final String tenancy;

    public ContextUser(String username, String password, Collection<? extends GrantedAuthority> authorities,
            String tenancy) {
        super(username, password, authorities);
        this.tenancy = tenancy;
    }

    public String getTenancy() {
        return tenancy;
    }
}

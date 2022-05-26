package com.trading.journal.authentication.jwt.data;

import java.io.Serial;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class ContextUser extends User {

    @Serial
    private static final long serialVersionUID = 2657274830115424036L;
    private final String tenancy;

    public ContextUser(String username, Collection<? extends GrantedAuthority> authorities,
            String tenancy) {
        super(username, "", authorities);
        this.tenancy = tenancy;
    }

    public String getTenancy() {
        return tenancy;
    }
}

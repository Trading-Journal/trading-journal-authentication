package com.trading.journal.authentication.authentication.service.impl;

import com.allanweber.jwttoken.data.ContextUser;
import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authentication.service.UserPasswordAuthenticationManager;
import com.trading.journal.authentication.password.service.PasswordService;
import com.trading.journal.authentication.tenancy.Tenancy;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class UserPasswordAuthenticationManagerImpl implements UserPasswordAuthenticationManager {

    private final UserRepository userRepository;

    private final PasswordService passwordService;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApplicationException(HttpStatus.UNAUTHORIZED, "Bad Credentials"));
        if (!user.getEnabled() || !user.getVerified()) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Locked Credentials");
        }

        ofNullable(user.getTenancy())
                .filter(tenancy -> tenancy.getEnabled().equals(false))
                .ifPresent(unused -> {
                    throw new ApplicationException(HttpStatus.FORBIDDEN, "Your tenancy is disabled by the system admin");
                });

        String password = (String) authentication.getCredentials();
        if (!passwordService.matches(password, user.getPassword())) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Bad Credentials");
        }

        List<SimpleGrantedAuthority> authorities = of(user).map(User::getAuthorities)
                .orElse(emptyList()).stream().map(userAuthorities -> new SimpleGrantedAuthority(userAuthorities.getAuthority().getName())).toList();

        if (authorities.isEmpty()) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "No Authorities");
        }

        Tenancy tenancy = ofNullable(user.getTenancy()).orElse(Tenancy.builder().build());
        ContextUser principal = new ContextUser(email, tenancy.getId(), tenancy.getName());
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }
}

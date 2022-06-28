package com.trading.journal.authentication.authentication.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authentication.service.UserPasswordAuthenticationManager;
import com.trading.journal.authentication.jwt.data.ContextUser;
import com.trading.journal.authentication.password.service.PasswordService;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Optional.of;

@Service
@RequiredArgsConstructor
public class UserPasswordAuthenticationManagerImpl implements UserPasswordAuthenticationManager {

    private final ApplicationUserRepository applicationUserRepository;

    private final PasswordService passwordService;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        ApplicationUser applicationUser = applicationUserRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(HttpStatus.UNAUTHORIZED, "Bad Credentials"));
        if (!applicationUser.getEnabled() || !applicationUser.getVerified()) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Locked Credentials");
        }

        String password = (String) authentication.getCredentials();
        if (!passwordService.matches(password, applicationUser.getPassword())) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Bad Credentials");
        }

        List<SimpleGrantedAuthority> authorities = of(applicationUser)
                .map(ApplicationUser::getAuthorities)
                .orElse(emptyList())
                .stream()
                .map(userAuthorities -> new SimpleGrantedAuthority(userAuthorities.getAuthority().getName())).toList();

        if (authorities.isEmpty()) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "No Authorities");
        }

        ContextUser principal = new ContextUser(email, applicationUser.getUserName());
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }
}

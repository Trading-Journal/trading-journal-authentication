package com.trading.journal.authentication.authentication.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authentication.service.UserPasswordAuthenticationManager;
import com.trading.journal.authentication.jwt.data.ContextUser;
import com.trading.journal.authentication.password.service.PasswordService;
import com.trading.journal.authentication.user.ApplicationUser;
import com.trading.journal.authentication.user.ApplicationUserRepository;
import com.trading.journal.authentication.userauthority.service.UserAuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class UserPasswordAuthenticationManagerImpl implements UserPasswordAuthenticationManager {

    private final ApplicationUserRepository applicationUserRepository;

    private final UserAuthorityService userAuthorityService;

    private final PasswordService passwordService;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        ApplicationUser applicationUser = applicationUserRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(HttpStatus.UNAUTHORIZED, "Bad Credentials"));
        if (!applicationUser.getEnabled()) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Locked Credentials");
        }

        String password = (String) authentication.getCredentials();
        if (!passwordService.matches(password, applicationUser.getPassword())) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Bad Credentials");
        }

        List<SimpleGrantedAuthority> authorities = userAuthorityService.loadListAsSimpleGrantedAuthority(applicationUser);
        if (ofNullable(authorities).map(List::isEmpty).orElse(true)) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "No Authorities");
        }

        ContextUser principal = new ContextUser(email, applicationUser.getUserName());
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }
}

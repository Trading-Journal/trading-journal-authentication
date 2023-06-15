package com.trading.journal.authentication.authentication.service.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.allanweber.jwttoken.data.ContextUser;
import com.allanweber.jwttoken.data.TokenData;
import com.allanweber.jwttoken.helper.JwtConstants;
import com.allanweber.jwttoken.service.JwtTokenProvider;
import com.allanweber.jwttoken.service.JwtTokenReader;
import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.authentication.service.AuthenticationService;
import com.trading.journal.authentication.user.User;
import com.trading.journal.authentication.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenReader jwtTokenReader;

    @Override
    public LoginResponse signIn(Login login) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.email(), login.password()));
        ContextUser principal = (ContextUser) authenticate.getPrincipal();
        User user = userService.getUserByEmail(principal.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        TokenData accessToken = jwtTokenProvider.generateAccessToken(user);
        TokenData refreshToken = jwtTokenProvider.generateRefreshToken(user);
        return new LoginResponse(
                JwtConstants.TOKEN_TYPE,
                accessToken.token(),
                refreshToken.token(),
                accessToken.issuedAt(),
                user.getEmail());
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        String email = validateRefreshTokenAndGetUserEmail(refreshToken);
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        TokenData accessToken = jwtTokenProvider.generateAccessToken(user);
        return new LoginResponse(
                JwtConstants.TOKEN_TYPE,
                accessToken.token(),
                refreshToken,
                accessToken.issuedAt(),
                user.getEmail());
    }

    private String validateRefreshTokenAndGetUserEmail(String refreshToken) {
        if (!jwtTokenReader.isTokenValid(refreshToken)) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Refresh token is expired");
        }
        AccessTokenInfo accessTokenInfo = jwtTokenReader.getTokenInfo(refreshToken);
        if (!accessTokenInfo.scopes().contains(JwtConstants.REFRESH_TOKEN)) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED,
                    "Refresh token is invalid or is not a refresh token");
        }
        return accessTokenInfo.subject();
    }
}

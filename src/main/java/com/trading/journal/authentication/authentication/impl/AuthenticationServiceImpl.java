package com.trading.journal.authentication.authentication.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.authentication.AuthenticationService;
import com.trading.journal.authentication.authentication.Login;
import com.trading.journal.authentication.authentication.LoginResponse;
import com.trading.journal.authentication.jwt.JwtTokenProvider;
import com.trading.journal.authentication.jwt.JwtTokenReader;
import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.jwt.data.TokenData;
import com.trading.journal.authentication.jwt.helper.JwtConstants;
import com.trading.journal.authentication.user.ApplicationUserService;
import com.trading.journal.authentication.user.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final ApplicationUserService applicationUserService;
    private final ReactiveAuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenReader jwtTokenReader;

    @Override
    public Mono<LoginResponse> signIn(@Valid Login login) {
        return authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(login.email(), login.password()))
                .onErrorResume(throwable -> Mono.error(new AuthenticationServiceException(throwable.getMessage())))
                .map(Authentication::getPrincipal)
                .cast(UserDetails.class)
                .map(UserDetails::getUsername)
                .flatMap(applicationUserService::getUserByEmail)
                .map(applicationUser -> {
                    TokenData accessToken = jwtTokenProvider.generateAccessToken(applicationUser);
                    TokenData refreshToken = jwtTokenProvider.generateRefreshToken(applicationUser);
                    return new LoginResponse(
                            JwtConstants.TOKEN_TYPE,
                            accessToken.token(),
                            refreshToken.token(),
                            accessToken.issuedAt(),
                            applicationUser.getFirstName());
                });
    }

    @Override
    public Mono<LoginResponse> refreshToken(String refreshToken) {
        return validateRefreshTokenAndGetUserName(refreshToken)
                .flatMap(applicationUserService::getUserInfo)
                .map(UserInfo::getEmail)
                .flatMap(applicationUserService::getUserByEmail)
                .map(applicationUser -> {
                    TokenData accessToken = jwtTokenProvider.generateAccessToken(applicationUser);
                    return new LoginResponse(
                            JwtConstants.TOKEN_TYPE,
                            accessToken.token(),
                            refreshToken,
                            accessToken.issuedAt(),
                            applicationUser.getFirstName());
                });
    }

    private Mono<String> validateRefreshTokenAndGetUserName(String refreshToken) {
        return Mono.fromCallable(() -> {
            if (!jwtTokenReader.isTokenValid(refreshToken)) {
                throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Refresh token is expired");
            }
            AccessTokenInfo accessTokenInfo = jwtTokenReader.getRefreshTokenInfo(refreshToken);
            if (!accessTokenInfo.scopes().contains(JwtConstants.REFRESH_TOKEN)) {
                throw new ApplicationException(HttpStatus.UNAUTHORIZED,
                        "Refresh token is invalid or is not a refresh token");
            }
            return accessTokenInfo.userName();
        });
    }
}

package com.trading.journal.authentication.verification.service.impl;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.allanweber.jwttoken.helper.JwtConstants;
import com.allanweber.jwttoken.service.JwtTokenProvider;
import com.allanweber.jwttoken.service.JwtTokenReader;
import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.verification.service.HashProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class HashProviderJwt implements HashProvider {

    private final JwtTokenProvider jwtTokenProvider;

    private final JwtTokenReader jwtTokenReader;

    @Override
    public String generateHash(String value) {
        return jwtTokenProvider.generateTemporaryToken(value).token();
    }

    @Override
    public String readHashValue(String hash) {
        if (!jwtTokenReader.isTokenValid(hash)) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid hash value");
        }
        Optional<AccessTokenInfo> tokenInfo = ofNullable(jwtTokenReader.getTokenInfo(hash));
        if (tokenInfo.map(AccessTokenInfo::scopes).map(list -> list.size() > 1).orElse(true)
                || tokenInfo.map(AccessTokenInfo::scopes).orElse(emptyList()).stream().noneMatch(JwtConstants.TEMPORARY_TOKEN::equals)) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Hash is not in the right format");
        }
        return tokenInfo.map(AccessTokenInfo::subject).orElseThrow(() -> new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid hash content"));
    }
}

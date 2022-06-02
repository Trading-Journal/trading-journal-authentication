package com.trading.journal.authentication.verification.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.jwt.helper.JwtConstants;
import com.trading.journal.authentication.jwt.service.JwtTokenProvider;
import com.trading.journal.authentication.jwt.service.JwtTokenReader;
import com.trading.journal.authentication.verification.service.HashProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HashProviderJwt implements HashProvider {

    private final JwtTokenProvider provider;

    private final JwtTokenReader reader;

    @Override
    public String generateHash(String value) {
        return provider.generateTemporaryToken(value).token();
    }

    @Override
    public String readHashValue(String hash) {
        if (!reader.isTokenValid(hash)) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid hash value");
        }
        AccessTokenInfo tokenInfo = reader.getTokenInfo(hash);
        if (tokenInfo.scopes().size() > 1 && tokenInfo.scopes().stream().noneMatch(JwtConstants.TEMPORARY_TOKEN::equals)) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Hash is not in the right format");
        }
        return Optional.of(tokenInfo).map(AccessTokenInfo::subject).orElseThrow(() -> new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid hash content"));
    }
}

package com.trading.journal.authentication.jwt.impl;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.trading.journal.authentication.jwt.JwtTokenReader;
import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.jwt.helper.JwtConstants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.BindingContext;

@ExtendWith(SpringExtension.class)
public class AccessTokenResolverTest {

    @Mock
    MethodParameter parameter;

    @Mock
    BindingContext bindingContext;

    @Mock
    JwtTokenReader tokenReader;

    @InjectMocks
    AccessTokenResolver accessTokenResolver;

    @DisplayName("Given a access token resolve into AccessTokenInfo")
    @Test
    public void retrieveTokenInfo() {

        String token = UUID.randomUUID().toString();

        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/foo/foo")
                .header(HttpHeaders.AUTHORIZATION, JwtConstants.TOKEN_PREFIX.concat(token)));

        when(tokenReader.getAccessTokenInfo(token)).thenReturn(
                new AccessTokenInfo(
                        "UserAdm",
                        "tenancy_1",
                        singletonList("USER")));

        AccessTokenInfo tokenInfo = (AccessTokenInfo) accessTokenResolver
                .resolveArgument(parameter, bindingContext, exchange).block();

        assertThat(tokenInfo.userName()).isEqualTo("UserAdm");
        assertThat(tokenInfo.tenancy()).isEqualTo("tenancy_1");
        assertThat(tokenInfo.roles()).containsExactly("USER");
    }
}

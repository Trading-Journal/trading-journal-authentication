package com.trading.journal.authentication.jwt.impl;

import com.trading.journal.authentication.jwt.helper.JwtConstants;
import com.trading.journal.authentication.jwt.service.JwtResolveToken;
import com.trading.journal.authentication.jwt.service.impl.JwtResolveTokenHttpHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class JwtResolveTokenHttpHeaderTest {

    JwtResolveToken jwtResolveToken;

    @BeforeEach
    public void setUp() {
        jwtResolveToken = new JwtResolveTokenHttpHeader();
    }

    @Test
    @DisplayName("Given server request with token return token value")
    void requestWithToken() {
        String token = UUID.randomUUID().toString();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(JwtConstants.TOKEN_PREFIX.concat(token));
        String resolved = jwtResolveToken.resolve(request);
        assertThat(resolved).isEqualTo(token);
    }

    @Test
    @DisplayName("Given server request without token return null")
    void requestWithoutToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String resolved = jwtResolveToken.resolve(request);
        assertThat(resolved).isNull();
    }
}
